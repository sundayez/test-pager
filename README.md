## Test Pager - Bernardino Domínguez

Exercise for the application to Aircall

Code available [here](https://github.com/aircall/technical-test-pager)

To run the tests, execute the command ```./gradlew build```

### Remarks

- The architecture of the domain logic is done with ports & adapters approach
- The adapters are not implemented. Those ones that call the domain logic are simulated with polling logic. In a real case, message brokers or API calls could be used
- The PersistenceAdapter has not been used due to limited time, but a suggested use case would be to log the events in the system, or keep a historic record.
- Regarding databases, no constraints arise for this implementation. Anyway, a good DB need to deal with concurrency and guarantee a good state after an ordered set of operations.

### Assumptions

- When more than one alert arrives at the same time (using the poll of the AlertingAdapter), only the first one is used to send notifications
- When an alert has many targets in the same level, we deactivate the alert when ONE of the targets sends the acknowledgment. If a more constrained logic is needed, a strategy pattern can be used to decide when the alert is acknowledged.

### Tests

- Tests have been provided for all the services.
- Use cases 1, 2 and 4 involve one service, and 3 and 5 involve more than one service.
- Due to that, pay special attention to the tests in:
  - AlertManagerImplTest class: It includes use cases 1,2 and 4, and partially 3 and 5
  - DomainLogicAppTest class: These tests are end to end for cases 3 and 5. For time reasons, no end to end tests for cases 1,2 and 4 are provided, but they could be included in this class as well.