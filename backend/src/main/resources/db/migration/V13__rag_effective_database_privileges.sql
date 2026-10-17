-- PUBLIC's default TEMP privilege also reaches NOINHERIT roles. Direct role
-- revokes alone cannot deny temporary DDL. Preserve the database owner's rights.
DO $$
BEGIN
    EXECUTE format('REVOKE TEMPORARY ON DATABASE %I FROM PUBLIC',current_database());
    EXECUTE format('REVOKE CREATE,TEMPORARY ON DATABASE %I FROM rag_worker,rag_runtime',current_database());
    IF has_database_privilege('rag_worker',current_database(),'CREATE')
       OR has_database_privilege('rag_worker',current_database(),'TEMP')
       OR has_database_privilege('rag_runtime',current_database(),'CREATE')
       OR has_database_privilege('rag_runtime',current_database(),'TEMP') THEN
        RAISE EXCEPTION 'Unsafe effective runtime database privileges require operator repair';
    END IF;
END $$;
