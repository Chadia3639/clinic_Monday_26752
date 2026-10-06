# Clinic Management – Class Practical Exercise 2 (Model the Clinic)

Spring Boot 4.1.1 · Java 17+ · Spring Data JPA · MySQL · `ddl-auto=update` · no DTOs, no validation.

## 1. Run it

1. Start your database and set `username` / `password` in `src/main/resources/application.properties`.
   * **PostgreSQL** (configured): user `postgres`, create the database first with `CREATE DATABASE clinic_db;`.
   * **MySQL**: replace the datasource lines with `jdbc:mysql://localhost:3306/clinic_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`.
2. `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`) – or run `ClinicmanagementApplication` from VS Code.
3. Open MySQL Workbench → schema `clinic_db` → refresh → the tables appear.

## 2. Model analysis (slide steps 1 & 2)

| Relationship | Type | Where the FK lives | Implementation |
|---|---|---|---|
| Doctor – Office | **1-1** ("exactly one office") | `doctors.office_id` (NOT NULL, UNIQUE) | `@OneToOne @JoinColumn` in `Doctor`; `mappedBy` in `Office` |
| Doctor – Specialization | **N-N** | join table `doctor_specialization(doctor_id, specialization_id)` | `@ManyToMany @JoinTable` in `Doctor`; `mappedBy` in `Specialization`. Pure link, **no extra data → not promoted** |
| Patient – Doctor | **N-N** (a patient books with many doctors, a doctor sees many patients) | — | **Promoted to entity `Appointment`** because the link has its own data: date, reason, status |
| Patient – Appointment | **1-N** | `appointments.patient_id` | `@ManyToOne` in `Appointment` |
| Doctor – Appointment | **1-N** | `appointments.doctor_id` | `@ManyToOne` in `Appointment` |

Rule used: the FK always lives on the "many" side; for 1-1 it goes on the side that cannot exist without the other
(the doctor must have an office); an N-N becomes an entity only when the relationship carries attributes.

## 3. Generated tables (slide step 4 – explain each column)

* **offices**: `id` PK · `room_number` unique · `floor_number` · `building`
* **doctors**: `id` PK · `first_name` · `last_name` · `email` unique · `phone` · `office_id` FK→offices (unique ⇒ enforces 1-1)
* **specializations**: `id` PK · `name` unique · `description`
* **doctor_specialization**: `doctor_id` FK→doctors · `specialization_id` FK→specializations (together = composite PK; one row per link)
* **patients**: `id` PK · `first_name` · `last_name` · `email` unique · `phone` · `date_of_birth`
* **appointments**: `id` PK · `appointment_date` · `reason` · `status` (stored as text: SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW) · `patient_id` FK→patients · `doctor_id` FK→doctors

## 4. REST API

### Specializations (CRUD)
| Method | URL | Notes |
|---|---|---|
| POST | `/api/specializations` | `{"name":"Cardiology","description":"Heart"}` – 409 if name exists |
| GET | `/api/specializations` | list |
| GET | `/api/specializations/{id}` | |
| GET | `/api/specializations/search?keyword=card` | query |
| PUT | `/api/specializations/{id}` | |
| DELETE | `/api/specializations/{id}` | detaches it from doctors first (doctors are kept) |
| GET | `/api/specializations/{id}/doctors` | N-N from the other side |

### Doctors (CRUD + N-N)
| Method | URL | Notes |
|---|---|---|
| POST | `/api/doctors` | office is mandatory; specializations by id |
| GET | `/api/doctors`, `/api/doctors/{id}` | |
| PUT | `/api/doctors/{id}` | updates fields + office; `specializations` array (if sent) replaces the set |
| DELETE | `/api/doctors/{id}` | 409 if the doctor still has appointments; the office is deleted with the doctor |
| PUT | `/api/doctors/{doctorId}/specializations/{specId}` | add one specialization |
| DELETE | `/api/doctors/{doctorId}/specializations/{specId}` | remove one specialization |

### Queries
| URL | Meaning |
|---|---|
| `GET /api/doctors/by-specialization?name=Cardiology` | doctors with a given specialization |
| `GET /api/doctors/search?lastName=smi` | doctors by last name |
| `GET /api/doctors/by-office?room=A-101` | doctor of an office |
| `GET /api/doctors/multi-specialized?min=2` | doctors with ≥ N specializations |
| `GET /api/doctors/{id}/appointments` | appointments of a doctor |
| `GET /api/doctors/{id}/patients` | distinct patients seen by a doctor |
| `GET /api/appointments/by-status?status=SCHEDULED&doctorId=1` | by status (doctorId optional) |
| `GET /api/appointments/range?from=2026-10-01T00:00:00&to=2026-10-31T23:59:59` | by date range |
| `GET /api/appointments/workload` | number of appointments per doctor |
| `GET /api/patients/{id}/appointments` | appointments of a patient |

### Patients, Appointments, Offices
* `/api/patients` – POST, GET, GET `/{id}`, PUT, DELETE, GET `/search?lastName=`
* `POST /api/appointments?patientId=1&doctorId=1` (body: `appointmentDate`, `reason`, optional `status`) – refuses a doctor double-booking
* `GET/PUT/DELETE /api/appointments/{id}`, `PATCH /api/appointments/{id}/status?status=CANCELLED`
* `GET /api/offices`, `GET /api/offices/{id}` (read-only; offices live and die with their doctor)

## 5. Quick test (in this order)

```bash
curl -X POST localhost:1014/api/specializations -H "Content-Type: application/json" \
  -d '{"name":"Cardiology","description":"Heart and blood vessels"}'
curl -X POST localhost:1014/api/specializations -H "Content-Type: application/json" \
  -d '{"name":"Pediatrics","description":"Children"}'

curl -X POST localhost:1014/api/doctors -H "Content-Type: application/json" -d '{
  "firstName":"Alice","lastName":"Smith","email":"alice@clinic.com","phone":"0788000001",
  "office":{"roomNumber":"A-101","floorNumber":1,"building":"Main"},
  "specializations":[{"id":1},{"id":2}]}'

curl -X POST localhost:1014/api/patients -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@mail.com","phone":"0788000002","dateOfBirth":"1995-04-12"}'

curl -X POST "localhost:1014/api/appointments?patientId=1&doctorId=1" -H "Content-Type: application/json" \
  -d '{"appointmentDate":"2026-10-20T09:30:00","reason":"Chest pain check-up"}'

curl "localhost:1014/api/doctors/by-specialization?name=Cardiology"
curl localhost:1014/api/appointments/workload
```

## 6. Design notes
* Entities are returned directly (no DTOs), so the inverse sides (`Office.doctor`, `Specialization.doctors`,
  `Doctor.appointments`, `Patient.appointments`) are `@JsonIgnore`d to avoid infinite JSON loops; use the dedicated
  endpoints above to navigate them.
* Errors are returned as JSON: 404 not found, 409 conflict (duplicate / still in use / double booking), 400 malformed request.

## 7. Troubleshooting
* **"cannot find symbol getXxx()/setXxx()"** – Lombok is not running. The `pom.xml` declares it explicitly (needed on JDK 23+);
  in VS Code run *Java: Clean Java Language Server Workspace*.
* **"Access denied for user" / "password authentication failed"** – wrong DB password in `application.properties`.
* **"Communications link failure" / "Connection refused"** – the database service is not running or the port is wrong (MySQL 3306, PostgreSQL 5432).
* **VS Code Java extension crashes (`hs_err_pid*.log`, "Out of Memory ... Native memory allocation failed")** – it is the
  language server, not the project. Delete the `hs_err_pid*.log` / `replay_pid*.log` files, close heavy apps, make sure
  Windows has a page file (System > Advanced > Performance > Virtual memory > *Automatically manage*), and in VS Code
  settings set `java.jdt.ls.vmargs` to `-XX:+UseParallelGC -Xmx1G -Xms256m`, then run *Clean Java Language Server Workspace*.
