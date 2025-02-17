local cnt = redis.call('incr', KEYS[1]);
if (tonumber(cnt) > tonumber(ARGV[1])) then
    redis.call('decr', KEYS[1]);
    return 0;
else
    return 1;
end