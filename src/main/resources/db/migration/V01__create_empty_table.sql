create table date_example (
    id bigserial primary key,
    timestamp_with_zone timestamp with time zone,
    timestamp_without_zone timestamp without time zone,
    timestamp_with_utc_offset timestamp with time zone
)