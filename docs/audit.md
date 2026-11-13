# Audit log

Airavata records every change made to a record: who made it, what they did, and to which record. Administrators read the log from the **Audit log** page of the dashboard or from the API.

## What is recorded

One event is written each time a record is created, updated or deleted, whatever caused it: a person using the dashboard, a script holding a token, or Airavata itself.

| Field | Meaning |
|---|---|
| `occurredAt` | When it happened, in milliseconds since the epoch. |
| `actor` | Who did it: a user id such as `cilogon:12345`, `root`, a service account `sa:<name>`, or `system`. |
| `action` | `CREATE`, `UPDATE`, `DELETE`, or `UPSERT` for a write that inserts the record or updates it if it exists. |
| `entityType` | The kind of record, such as `processes`, `groups` or `scp_data_storages`. |
| `entityId` | The record's id. |
| `changedFields` | For an update that named its columns, which ones. |

`system` is the actor for changes with no person behind them, such as Airavata moving a process from one status to the next as its job progresses.

The log records **that** a record changed, never the values. Records hold secrets such as SSH passwords and token digests, and the log would otherwise be a second place to protect them. To see what a record contains now, read it. A deleted record's last contents are not kept.

Reading is not recorded, and neither is a request that fails or changes nothing.

An event is written in the same transaction as the change it describes. A change that is rolled back leaves no event, and a change cannot commit without one. Events are never edited or removed by Airavata, and they outlive the users and records they refer to.

When a record is deleted, anything the database removes along with it, such as a group's memberships, is not listed separately. Only the record you deleted has an event.

## Reading the log

Reading it needs the `audit:read` permission, which `ADMIN` and `SUPER_ADMIN` have by default, because the log names what every user did. See [Permissions](auth.md#permissions).

```
GET /api/v1/audit-events
```

Events come back newest first. Every filter is optional and they combine.

| Parameter | Meaning |
|---|---|
| `userId` | Only events by this actor. |
| `entityType` | Only events on this kind of record. |
| `from` | Only events at or after this time (milliseconds since the epoch). |
| `to` | Only events before this time. Must be after `from`. |
| `limit` | Page size, 1 to 500. Defaults to 100. |
| `before` | The previous response's `nextCursor`, to read the next page. |

```bash
# What did alice change on 11 November 2026 (UTC)?
curl -s -H "Authorization: Bearer $TOKEN" \
  "localhost:9095/api/v1/audit-events?userId=cilogon:12345&entityType=processes&from=1794355200000&to=1794441600000"
```

```json
{
  "items": [
    {
      "id": 4182,
      "occurredAt": 1794390211042,
      "actor": "cilogon:12345",
      "action": "CREATE",
      "entityType": "processes",
      "entityId": "7f87d3e9-f81c-4d44-bad0-0a8c3e8393e7",
      "changedFields": ""
    }
  ],
  "nextCursor": null
}
```

When `nextCursor` is not null there are more events: pass it as `before` to get them. `GET /api/v1/audit-events/entity-types` lists the values `entityType` accepts.

## In the dashboard

Administrators see **Audit log** under the admin pages. Filter by user id, record type and date range, then page back through older events.
The date range uses your browser's time zone.
