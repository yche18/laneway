-- Check that the thread identifier matches the lock owner.
if(redis.call('get', KEYS[1]) ==  ARGV[1]) then
    -- Release the lock: DEL key.
    return redis.call('del', KEYS[1])
end
return 0