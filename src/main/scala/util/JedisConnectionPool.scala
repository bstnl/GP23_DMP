package util

import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

/**
  * redis 连接
  */
object JedisConnectionPool {
  val config = new JedisPoolConfig()
  config.setMaxIdle(20)
  config.setMaxIdle(10)
  private val pool = new JedisPool(config,"hadoop01",6379,1000)

  def getConnection():Jedis={
    pool.getResource
  }
}
