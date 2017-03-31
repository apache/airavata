#Quick guide to VirtualBox Installation:

To try this out on VirtualBox:

1. Get a copy of CentOS7: http://isoredirect.centos.org/centos/7/isos/x86_64/CentOS-7-x86_64-Minimal-1511.iso
2. Install VirtualBox
3. Enable host-only networking! Go to File->Preferences->Networks->Host-only and hit "add" (on the right, with a + icon)
4. create a new VM in VirtualBox (The big blue "new" button)
5. Choose "Linux" and "Redhat 64bit" for the first parameters
6. Go through the system specs process (2GB RAM and 10GB disk space should be fine)
7. After that, go to "settings", add a second network interface, and enable a host-only interface
8. Then, select Storage->Optical Drive, and choose the CentOS iso that you just downloaded.
9. Start your VM! Begin the CentOS installation. Don't forget your root password!!!
10. When you log in the first time, you'll have to make sure of two things:
..* set the hostname! use the command "hostnamectl set-hosthame myhostname.localdomain"
..* make sure your ethernet interfaces working & will come back on boot (in /etc/sysconfig/network-scripts/ifcfg-enp0s? change onboot=no to onboot=yes)
11. check interface names with "ip addr" and bring them up with ifup enp0s3 (for example)

You should be able to ssh in to your VM from your standard terminal - the interface through VirtualBox is pretty rough, and hard to get nice features like copy-paste from your host machine working.

Now, yum install git, clone this repo, and let me know how it goes!

The current inventory assumes 3 VMs with host-only ips set as:
192.168.56.98
192.168.56.99
192.168.56.100
