# Submission Notes

## Time Spent

Approximately 3 hours, including implementation, debugging, automated testing, concurrency handling, and API validation.

## Assumptions

- The application uses in-memory storage as required by the assignment.
- The four provided plans are seeded when the application starts.
- A membership purchase becomes active after successful payment.
- An unlimited membership remains active for the lifetime of the application because cancellation and membership lifecycle management are outside the required scope.
- The Launch Promo Pack has exactly five available slots as provided in the seed data.
- Monetary values are represented as integer cents.
- Membership start dates are represented using `LocalDate` in `YYYY-MM-DD` format.
- The provided `PaymentGateway` abstraction represents the external payment system.

## API / Design Choices

### API Endpoints

Implemented the three required endpoints:

- `GET /v1/plans`
- `POST /v1/memberships/purchases`
- `GET /v1/memberships/purchases/{id}`

### Layered Design

The application follows a simple layered architecture:

- **Controller** — handles HTTP requests and responses.
- **Service** — contains membership purchase business rules.
- **Store** — provides in-memory persistence.
- **Domain** — represents plans and membership purchases.
- **DTO** — separates API request/response models from domain objects.
- **Payment Gateway** — abstracts payment processing.

Constructor-based dependency injection is used throughout the application.

### Design Principles & Coding Practices

The implementation follows the following principles and practices:

- **Separation of Concerns** — controllers, business logic, persistence, payment integration, and domain models have clearly separated responsibilities.
- **Single Responsibility** — classes are kept focused on a specific responsibility, such as handling HTTP requests, applying purchase rules, or managing in-memory state.
- **Dependency Inversion** — the membership service depends on the `PaymentGateway` interface rather than a concrete payment implementation.
- **DTO Pattern** — request and response DTOs are used to keep API contracts separate from internal domain models.
- **Constructor Dependency Injection** — dependencies are provided through constructors rather than being created inside business classes.
- **Centralized Exception Handling** — `GlobalExceptionHandler` provides consistent API error responses for business and validation failures.
- **Meaningful Domain Methods** — operations such as `reserveSlot()`, `releaseSlot()`, and `reserveUnlimitedMembership()` encapsulate business-related state changes.
- **Immutable Purchase Model** — `MembershipPurchase` uses immutable fields to prevent accidental modification after creation.
- **Thread-Safe State Management** — `ConcurrentHashMap` is used for shared in-memory state, with atomic operations used where required.
- **Safe Monetary Representation** — monetary values are represented as integer cents rather than floating-point values.
- **Compensating Rollback** — reservations made before payment are released when payment fails.

The design intentionally remains lightweight and focused on the core requirements of the assignment rather than introducing unnecessary infrastructure or abstractions.

### Concurrency

Promo inventory is protected using synchronized slot reservation logic so concurrent requests cannot oversell the available five promo slots.

`ConcurrentHashMap` is used for in-memory storage, and atomic `putIfAbsent` is used to prevent multiple active unlimited memberships for the same member.

### Payment Failure and Rollback

Inventory and membership reservations are made before payment is charged.

If payment fails, the reservations are released and the purchase is not stored.

This ensures that a failed payment does not consume promo inventory or permanently reserve an unlimited membership.

### Error Handling

A global exception handler provides consistent API error responses for cases such as:

- Unknown plan
- Inactive plan
- Duplicate active unlimited membership
- Promo sold out
- Payment failure
- Unknown purchase
## Tests

Automated tests cover the following behaviours:

- All four plans are returned by `GET /v1/plans`.
- The inactive Legacy Plan is included in the plan response.
- The Launch Promo Pack starts with five remaining slots.
- A successful membership purchase returns `201 Created`.
- A created purchase can be retrieved using its ID.
- An unknown purchase returns `404`.
- A blank member name is rejected.
- An invalid start date is rejected.
- An unknown plan is rejected.
- An inactive plan is rejected.
- A member cannot have multiple active unlimited memberships.
- Five promo purchases can succeed.
- A sixth promo purchase is rejected.
- Ten concurrent promo purchase attempts result in exactly five successful purchases.
- Promo inventory does not become negative.
- Payment failure does not create a purchase.
- Payment failure does not consume promo inventory.
- Payment failure releases an unlimited membership reservation, allowing the member to retry successfully.

## Known Gaps / What I Would Do Next

The implementation focuses on the required scope of the assignment.

Possible future improvements include:

- Persistent database storage.
- Membership cancellation and lifecycle management.
- Idempotency support for purchase requests.
- Pagination for purchase history.
- More detailed payment failure categories.
- Production-grade metrics and observability.
- Authentication and authorization.

These features were outside the required core scope and were therefore not implemented.

## AI Usage

AI assistance was used during development for:

- Reviewing the assignment requirements.
- Discussing API and package design.
- Reviewing concurrency and payment rollback logic.
- Creating and refining automated tests.
- Debugging Spring Boot and Gradle test configuration.
- Reviewing code quality and submission completeness.
- Adding structured logging across the controller, service, and store layers.