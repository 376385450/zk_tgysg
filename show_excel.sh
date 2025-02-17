
for i in $(seq 1 1000); do 
ls -alh /proc/3461045/fd | grep -v jar | grep -v ttf  | grep -v anon_inode | grep -v socket;
sleep 1
done 

