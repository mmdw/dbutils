
DECLARE
	res CLOB; 
BEGIN 
    set autotrace on;
    SELECT count(*)  INTO res from oeso_group_permission ;
END;
/