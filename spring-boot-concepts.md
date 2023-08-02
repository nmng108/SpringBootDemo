# I. Basic
## 1. Repository pattern & Unit of Work

Refine persistence layer into 3 layers:

a. Domain Model layer: contains aggregates which group classes relating to each others in a specific domain.

b. Infrastructure-Persistence layer:

- **Repository**: maps 1 aggregate root to 1 repository.
    - define database queries & commands

- __Unit of Work__: a pattern that helps transform db queries of a domain activity into a transaction.
    - Benefits: executing multiples related db commands in a transaction helps mitigate cost at re-opening db connections or running discrete commands.

c. Data layer (or tier)


## 2. JPA, ORM

- **Entity**:  


## 4. Service


## 5. Model

## 6. Learn the structure of a typical Spring Boot project and sample code of existing projects.