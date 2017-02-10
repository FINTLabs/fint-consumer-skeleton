const uuid = require('uuid')
const amqp = require('amqplib')

console.log('Usage: npm start <orgId>')

const config = {
  host: 'localhost',
  user: 'guest',
  password: 'guest',
  vhost: '',
  orgId: process.argv[2] === undefined ? 'rogfk.no' : process.argv[2]
}
console.log(`Using orgId: ${config.orgId}`)

const reply = {
  corrId: '',
  action: 'HEALTH_CHECK',
  status: "PROVIDER_ACCEPTED",
  time: new Date().getTime(),
  orgId: config.orgId,
  source: "fk",
  client: "vfs",
  message: null,
  data: [ 'Reply from test-client' ]
}

const connectionString = `amqp://${config.user}:${config.password}@${config.host}:5672/${config.vhost}`
const queue = `${config.orgId}.downstream`


const consumeMsg = (channel) => {
  return channel.assertQueue(queue, { durable: true }).then((ok) => {
    return channel.consume(queue, (msg) => {
      console.log(msg)
      reply.corrId = uuid()
      channel.publish('', msg.properties.replyTo, new Buffer(JSON.stringify(reply)), { 'contentType': 'application/json' })
      channel.ack(msg)
    })
  })
}

const connection = amqp.connect(connectionString)
connection.then((con) => {
  return con.createChannel()
}).then((channel) => {
  return consumeMsg(channel)
}).catch(console.warn)
