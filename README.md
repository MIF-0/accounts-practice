# accounts-practice

Gradle multi-module structure help structure the code and keep dependencies where they belong,
so you can't accidentally or intentionally use those where you shouldn't.
In addition it make it easier to change something specific without touching other part of the application.

I know that overall app is small and I could go with just separate packages.
But I experience that usually it ends badly.
As you mentioned production quality code, it also means maintainable and ready for a future changes.
So it worth to spend additional time to set up thing properly, then after pay the price of coupled code.

# Decisions
Decided to go with optimistic locking approach. Concurrent hash map and ifPresent, ifAbsent should help with this.
Why it is better? because in real world there is not a lot of concurrent requests to the same account, so with optimistic locking we would reduce time.
If there is a lot of concurrent requests to the same account, optimistic locking can do more harm, as it would mean more retries.

Try to use approach with returning result instead of throwing errors (I tried different possibilities, so sorry for inconsistency)
Generally speaking I like it more as it makes API more clear, but with Java it is much harder to implement

# Limitation
This app supports for now only GPB (there is nothing wrong with supporting other currencies,
but it would require pre-create company account), so the limitation is only on RestApi level,
customer can't provide currency.
Not all test written, but I tried to cover main things.
There is some duplication and generally a lot of improvement can be done,
especially with events (which should be used, but they are not in this implementation)
Didn't have enough time to properly think transfers through

# Domain
Domain models are immutable, which help us to worry less about concurrent access to the same object
Account context:
DomainModel: Account
Properties: id, balance
Behaviour: debit, withdraw money

Operation context:

DomainModel: TopUp(?)
Properties: id, account, status, created_at,
Behaviour: mark as done
Money should not magically appear, they would be transfer from company account

DomainModel: InternalTransfer
Properties: id, accountFrom, accountTo, status, created_at
Behaviour: withdraw done, debit done

DomainModel: ExternalTransfer
Properties: id, accountFrom, address, status, created_at
Behaviour: withdraw done, debit done

First two operations can be sync. But the third one should be async as we are dealing with external service

DomainService: Operations
1. Register operation
2. Consumer AccountBalanceUpdated

DomainService: Accounts
1. Handle openAccount Operation
2. Produce AccountCreated domain events
2. Produce AccountBalanceUpdated domain events

## Library used
- Junit, Mockito - for testing
- jcstress - to concurrency test (you can run `./gradlew jcstress`)
- logback + sel4j for logging
- spotless to maintain same code style
- helidon as Rest Framework

## API example
`http://localhost:9080/observe/health` - to observe health
`http://localhost:9080/hello` - default
`curl --request POST --url http://localhost:9080/account/open-account -H "Content-Type: application/json" --data '{"name":"My name"}'`
`curl --request GET --url http://localhost:9080/account/95fa17df-8d41-44e4-94c5-178f93d7858f -H "Content-Type: application/json"`
`curl --request POST --url http://localhost:9080/account/95fa17df-8d41-44e4-94c5-178f93d7858f/transfer/top-up -H "Content-Type: application/json" --data '{"amount":"100"}'`
`curl --request POST --url http://localhost:9080/account/95fa17df-8d41-44e4-94c5-178f93d7858f/transfer/internal -H "Content-Type: application/json" --data '{"amount":"100", "account_to":"c70baed6-1ec0-4549-9e19-924820023c11"}'`
`curl --request POST --url http://localhost:9080/account/95fa17df-8d41-44e4-94c5-178f93d7858f/transfer/external -H "Content-Type: application/json" --data '{"amount":"100", "address_to":"some_address"}'`
`curl --request GET --url http://localhost:9080/account/946871e7-2ee0-47dc-b829-290440aa8cca/transfer -H "Content-Type: application/json"
`
