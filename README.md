# accounts-practice

Gradle multi-module structure help structure the code and keep dependencies where they belong,
so you can't accidentally or intentionally use those where you shouldn't.
In addition it make it easier to change something specific without touching other part of the application.

I know that overall app is small and I could go with just separate packages.
But I experience that usually it ends badly.
As you mentioned production quality code, it also means maintainable and ready for a future changes.
So it worth to spend additional time to set up thing properly, then after pay the price of coupled code.

## Library used
- Junit, Mockito - for testing
- logback + sel4j for logging
- spotless to maintain same code style
