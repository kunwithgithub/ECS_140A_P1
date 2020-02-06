procedure Test is

             MAX : constant := 30;
             Type MAX_INDEX is range 1 .. MAX;
             I : INTEGER;
             
             procedure ADD ( X: in INTEGER) is
                   I : INTEGER;
                   begin
                   I := 1; 
                   while I <= MAX loop
                     I:= I + 1;
                   end loop;
             end ADD;
         begin 
            I := 1;
            ADD(I);
end Test;
