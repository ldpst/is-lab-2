CREATE OR REPLACE FUNCTION "is-lab-2".delete_tickets_by_venue(
    p_venue_id BIGINT
)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    affected BIGINT;
BEGIN
    DELETE FROM "is-lab-2".tickets
    WHERE venue_id = p_venue_id;

    GET DIAGNOSTICS affected = ROW_COUNT;

    RETURN affected;
END;
$$;


CREATE OR REPLACE FUNCTION "is-lab-2".count_tickets_by_venue_capacity_greater(
    p_capacity BIGINT
)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    affected BIGINT;
BEGIN
    SELECT COUNT(*)
    INTO affected
    FROM "is-lab-2".tickets t
    JOIN "is-lab-2".venues v ON v.id = t.venue_id
    WHERE v.capacity > p_capacity;

    RETURN affected;
END;
$$;


CREATE OR REPLACE FUNCTION "is-lab-2".unique_ticket_venues()
RETURNS TABLE (
    id BIGINT,
    name VARCHAR(255),
    capacity INTEGER,
    address_street VARCHAR(255),
    address_zip_code VARCHAR(255)
)
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    RETURN QUERY
        SELECT v.id, v.name, v.capacity, v.address_street, v.address_zip_code
        FROM "is-lab-2".venues v
        WHERE EXISTS (
            SELECT 1
            FROM "is-lab-2".tickets t
            WHERE t.venue_id = v.id
        )
        ORDER BY v.id;
END;
$$;


CREATE OR REPLACE FUNCTION "is-lab-2".cancel_event(
    p_event_id BIGINT
)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    affected BIGINT;
BEGIN
    DELETE FROM "is-lab-2".tickets
    WHERE event_id = p_event_id;

    GET DIAGNOSTICS affected = ROW_COUNT;

    RETURN affected;
END;
$$;


CREATE OR REPLACE FUNCTION "is-lab-2".delete_tickets_by_person(
    p_person_id BIGINT
)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    affected BIGINT;
BEGIN
    DELETE FROM "is-lab-2".tickets
    WHERE person_id = p_person_id;

    GET DIAGNOSTICS affected = ROW_COUNT;

    RETURN affected;
END;
$$;
