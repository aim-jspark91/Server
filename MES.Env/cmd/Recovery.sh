#!/usr/bin/ksh
if [[ $# -eq 0 ]]
then
   echo 'Usage: Recovery.sh <fileName>' 
   exit
fi

cd /usr01/snsadm/Backup/MES

cp ./$1.tar.gz /usr01/snsadm/mes/

cd /usr01/snsadm/mes/

gzip -d $1.tar.gz

tar -xvf $1.tar

rm -rf $1.tar
