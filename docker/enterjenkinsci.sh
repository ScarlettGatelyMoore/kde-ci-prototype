PID=$(docker inspect --format {{.State.Pid}} kde-master)
nsenter --target $PID --mount --uts --ipc --net --pid
