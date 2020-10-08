#!/usr/bin/ksh

cd /usr01/snsadm/mes

today=`date +%Y%m%d%H%M%S`

path=/usr01/snsadm/Backup/MES/

tar -cvf "MES_"$today.tar lib/ cmd/ config/ cfg/

gzip "MES_"$today.tar

mv /usr01/snsadm/mes/MES*.tar.gz $path

rm -rf "MES_"$today.tar

num=`find $path -mtime +30 | wc -l`

echo "----------- Remove Old Backup Count : " $num "-----------"

find $path -mtime +30 | xargs rm -f

echo "----------- Success!! -----------"

