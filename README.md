# accounts-practice

Gradle multi-module structure help structure the code and keep dependencies where they belong,
so you can't accidentally or intentionally use those where you shouldn't.
In addition it make it easier to change something specific without touching other part of the application.

I know that overall app is small and I could go with just separate packages.
But I experience that usually it ends badly.
As you mentioned production quality code, it also means maintainable and ready for a future changes.
So it worth to spend additional time to set up thing properly, then after pay the price of coupled code.

Account context:
DomainModel: Account
Properties: id, balance
Behaviour: debit, withdraw money

Operation context:

DomainModel: TopUp(?)
Properties: id, account, status, created_at,
Behaviour: mark as done

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

Decided to go with optimistic locking approach. Concurrent hash map and ifPresent, ifAbsent should help with this.
Why it is better? because in real world there is not a lot of concurrent requests to the same account, so with optimistic locking we would reduce time.
If there is a lot of concurrent requests to the same account, optimistic locking can do more harm, as it would mean more retries.

## Library used
- Junit, Mockito - for testing
- jcstress - to concurrency test (you can run `./gradlew jcstress`)
- logback + sel4j for logging
- spotless to maintain same code style
- helidon as Rest Framework
