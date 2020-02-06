procedure Test is
	: constant := 30;
	type is range 1..MAX;
	procedure ADD(x:in INTEGER;) is
		I: INTEGER;
		begin
		I:=1
		while I <= MAX loop
			I := I+1;
		end loop;
	end ADD;
begin 
	I:=1;
	ADD(I);
	end Test;