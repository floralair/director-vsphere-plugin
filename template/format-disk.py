#!/usr/bin/python

# ***** BEGIN LICENSE BLOCK *****
# Copyright (c) 2013-2015 VMware, Inc. All Rights Reserved.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ***** END LICENSE BLOCK *****
"""Format and Mount Disks
"""
import sys
import os
import re
import threading
import commands
try:
  import json
except ImportError:
  import simplejson as json
from xml.dom.minidom import parseString

class formatSwapDisk(threading.Thread):
  def __init__(self, swap_disk):
    threading.Thread.__init__(self)
    self.swap_disk = swap_disk
  def run(self):
    print "Preparing Swap Disk"
    if os.path.exists(self.swap_disk):
      os.system(
      """
if [ -b %s ]; then
  swapoff -a
  file -s %s | grep swap
  if [ $? != 0 ]; then
    mkswap %s
  fi
  swapon %s
  # do not write to /etc/fstab since we do not want to auto-mount
  # swap disk when rebooting
fi
      """ % (self.swap_disk, self.swap_disk, self.swap_disk, self.swap_disk))

def write_file(file_name, content):
  os.system("[ -f %s ] && cp %s %s" % (file_name, file_name, file_name + "~"))
  f = open(file_name, 'w')
  f.write(content)
  f.close()

def get_volumes():
  try:
    machine_id = os.popen('/usr/sbin/vmware-rpctool machine.id.get').read()
    return json.loads(json.loads(machine_id)['volumes'])
  except:
    print "Failed to get volumes info"
    return (None, None)

def set_status(code, status):
  os.system("/usr/sbin/vmware-rpctool 'info-set guestinfo.disk.format.status %s'" % status);
  os.system("/usr/sbin/vmware-rpctool 'info-set guestinfo.DiskFormatCode %s'" % code);

def format_data_disks(dev2disk, mp2dev):
  print "Preparing Data Disks"
  logfile = '/tmp/serengeti-format-disks.log'
  format_script = '/tmp/serengeti-format-disks.sh'
  format_disks = ''
  for dev, disk in dev2disk.items():
    if not os.path.exists(disk) or os.path.exists(dev):
      continue
    format_disks += "format_disk " + disk + " " + dev + " & \n"

  if format_disks != '':
    format_script_content = """
function format_disk_internal()
{
  kernel=`uname -r | cut -d'-' -f1`
  first=`echo $kernel | cut -d '.' -f1`
  second=`echo $kernel | cut -d '.' -f2`
  third=`echo $kernel | cut -d '.' -f3`
  num=$[ $first*10000 + $second*100 + $third ]

  # we cannot use [[ "$kernel" < "2.6.28" ]] here because linux kernel
  # has versions like "2.6.5"
  if [ $num -lt 20628 ];
  then
    mkfs -t ext3 -b 4096 $1;
  else
    mkfs -t ext4 -b 4096 $1;
  fi;
}

function format_disk()
{
  flag=1
  while [ $flag -ne 0 ] ; do
    echo "Running sfdisk -uM $1. Occasionally it will fail due to device busy, we will re-run."

    # using fdisk to get disk size, the disk size formats are like below
    # Disk /dev/sda: 21.5 GB,
    # Disk /dev/sdb: 4295 MB,

    size_value=`fdisk -l ${1} | grep "Disk /dev/disk/by-id" |awk '{print $3}'`
    echo "Disk size value is ${size_value}"
    size_unit=`fdisk -l ${1} | grep "Disk /dev/disk/by-id" | awk '{print $4}' | cut -d',' -f1`
    echo "Disk size unit is ${size_unit}"

    # if the value is float, remove the "." and its decimal part
    size_integer_value=${size_value%%.*}
    echo "Disk size integer value is ${size_integer_value}"
    big_disk=0

    # the size unit can be KB, MB, GB, TB. For KB and MB, it is impossible to be larger than 2TB because
    # there is only four digits integers. So here we only care about GB and TB.
    if [ "${size_unit}" == "GB" ]; then
        if [ ${size_integer_value} -ge 2048 ]; then
            big_disk=1;
        fi
    elif [ "${size_unit}" == "TB" ]; then
        if [ ${size_integer_value} -ge 2 ]; then
            big_disk=1;
        fi
    fi

    # make disk partition according to disk size
    if [ $big_disk -ne 1 ]; then
        echo ",,L" | sfdisk -uM $1;
    else
        # keep optimal alignment for good performance
        # using gpt partition table
        # using 0%% 100%% to keep the alignment
        parted -a optimal -s $1 mklabel gpt -- mkpart primary 0%% 100%%;
    fi

    flag=$?
    sleep 3
  done

  flag=1
  while [ $flag -ne 0 ] ; do
    echo "Running mkfs $2. Occasionally it will fail due to device busy, we will re-run."
    echo "y" | format_disk_internal $2
    flag=$?
    sleep 3
  done
}

echo Started on `date`
%s
wait
echo Finished on `date`
echo
""" % format_disks
    write_file(format_script, format_script_content)
    #TODO: error handling
    os.system("/bin/bash " + format_script + " >> " + logfile + " 2>&1")

  # mount data disks
  for key in sorted(mp2dev.keys()):
    mp, dev = key, mp2dev[key]
    if not dev2disk.has_key(dev) or not os.path.exists(dev):
      print "Warning: device %s does not exist" % dev
      continue

    if not os.path.isdir(mp):
      os.mkdir(mp, 0755)

    fstype = ""
    dev_type_str = commands.getoutput("file -s " + dev)
    if re.compile('SGI XFS').search(dev_type_str):
      fstype = "xfs"
    elif re.compile('Linux.*ext2').search(dev_type_str):
      fstype = "ext2"
    elif re.compile('Linux.*ext3').search(dev_type_str):
      fstype = "ext3"
    else:
      fstype = "ext4"

    # Always mount the disk. If it's already mounted, nothing happens.
    # This can mount the new disks after bad disks are replaced.
    os.system("mount -t %s -o noatime %s %s" % (fstype, dev, mp))

    if os.system("grep -q %s /etc/mtab" % mp) != 0:
      # remove the bad mount point dir
      os.rmdir(mp)
      os.system("sed -i '/%s/d' /etc/fstab" % mp.replace("/", "\/"))
    elif os.system("grep -q '%s %s' /etc/fstab" % (dev, mp)) != 0:
      # remove bad entry
      os.system("sed -i '/%s/d' /etc/fstab" % mp.replace("/", "\/"))
      # add new entry
      os.system("echo %s\t\t%s\t\t%s\tdefaults,noatime\t0 0 >> /etc/fstab" % (dev, mp, fstype))

def main():
  volumes = get_volumes()
  if volumes is None:
    set_status("0", "Disks Ready")
    return

  set_status("1", "Preparing Disks")
  old_mp_to_new_mp = {}
  mount_point_to_device = {}
  device_to_disk = {}
  swap_disk = None
  index = 0
  for v in volumes:
    # disk uuid fetch from WS is: DATA:6000C298-2b5d-f41a-2581-1b07e74971e8
    # disk uuid shown in OS is: scsi-36000c2982b5df41a25811b07e74971e8
    # the logic partition is: scsi-36000c2982b5df41a25811b07e74971e8-part1
    uuid_in_os = "scsi-3" + re.split(":", v)[1].replace("-", "").lower()
    if v.startswith("DATA"):
      raw_disk = "/dev/disk/by-id/" + uuid_in_os
      device = raw_disk + "-part1"
      mount_point = "/mnt/data" + str(index)
      index += 1
      mount_point_to_device[mount_point] = device
      device_to_disk[device] = raw_disk
      old_mp_to_new_mp["/mnt/" + uuid_in_os + "-part1"] = mount_point
    else:
      swap_disk = "/dev/disk/by-id/" + uuid_in_os

  format_swap_disk_thread = formatSwapDisk(swap_disk)
  format_swap_disk_thread.start()

  format_data_disks(device_to_disk, mount_point_to_device)
  # Link old mount point to new mount point. It's for backward compatible with Serengti 2.1.0 in which the mount point is /mnt/{uuid_in_os}-part1
  for old_mp in old_mp_to_new_mp.keys():
    new_mp = old_mp_to_new_mp[old_mp]
    if os.path.exists(new_mp) and os.path.isdir(old_mp) and not os.path.islink(old_mp):
      os.rmdir(old_mp)
      os.symlink(new_mp, old_mp)

  format_swap_disk_thread.join()
  set_status("0", "Disks Ready")

if __name__ == '__main__':
  main()
