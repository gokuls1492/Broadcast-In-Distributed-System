#!/bin/bash


# Change this to your netid
netid=gxs161530

#
# Root directory of your project
PROJDIR=$HOME/AOS/Final
#
# This assumes your config file is named "config.txt"
# and is located in your project directory
#
CONFIG=$PROJDIR/config.txt

#
# Directory your java classes are in
#
#BINDIR= $HOME/AOS/test

# Your main project class
#
PROG=Init
n=1
#javac $PROJDIR/$PROG.java
cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
	read i
	read b
	read d
    while read -r line || [[ -n $line ]]; 
    do
		host=$( echo $line | awk '{ print $1 }' )
		domain=$host".utdallas.edu"
        ssh $netid@$domain java -cp $PROJDIR $PROG $n &
		
		n=$(( n + 1 ))
    done
   
)