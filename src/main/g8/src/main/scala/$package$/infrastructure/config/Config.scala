package $package$.infrastructure.config

case class DbConfig(username: String, password: String, url: String, driver: String, migrateOnStart: Boolean)

case class Config(db: DbConfig)
