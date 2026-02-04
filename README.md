
# 🚗 Split Ride – Backend

Split Ride is a backend system designed to **reduce travel costs by enabling reliable ride fare splitting**.

It helps users who are traveling in the same direction, around the same time, **form shared ride groups** so the cost of a ride can be divided among multiple participants.

----------

## 💰 The Problem It Solves

Splitting a ride fare sounds simple, but fails in practice when:

-   people join or leave unpredictably
-   groups grow too large or too small
-   timing is unclear
-   someone backs out at the last moment


When this happens, users end up:

-   paying more than expected
-   missing rides
-   or abandoning fare splitting altogether


----------

## 🎯 What This Backend Enables

The backend ensures that fare splitting is:

-   **fair** – only compatible users are grouped
-   **predictable** – groups have clear capacity and timing
-   **safe** – cancellations are handled without breaking others
-   **final** – once it’s time to travel, the group is locked in


This creates confidence that splitting the fare will actually reduce cost, not increase risk.

----------

## 🧩 How It Works (Conceptually)

-   Users declare their **ride intent** (where, when, flexibility)

-   The system forms **ride groups** with compatible users

-   Each group has:

    -   a limited size
    -   a shared time window
    -   a clear lifecycle

-   Groups automatically finalize when it’s time to travel


Users never need to manually coordinate or negotiate.

----------

## 🔄 What the System Manages for the User

-   Joining and leaving groups safely
-   Keeping group size within limits
-   Reopening groups if space becomes available
-   Cancelling groups that no longer make sense
-   Finalizing groups so fare splitting can proceed


All of this happens automatically.

----------

## 🚫 What This Backend Does Not Do

To stay focused on fare splitting, this backend does not:

-   calculate the actual fare
-   process payments
-   assign drivers or vehicles
-   track real-time locations


It provides the **coordination layer** needed before fare splitting can occur.

----------

## 🌱 Why This Matters

Fare splitting only saves money when coordination is reliable.

This backend exists to remove uncertainty so users can confidently choose to split rides — and pay less.