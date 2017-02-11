# FINT Consumer Skeleton

## Setup

The `SubscriberService` is added to the project to receive events.  
Custom code should be added in this class (or replaced by a new implementation) to alter the logic when new events are received.

## Test locally

To run a complete test setup locally use the `./gradlew runAll` task.  
This will start a local rabbitmq, a nodejs test-client that subsribes to and replies back for health check messages, and the fint-consumer Spring boot application.  