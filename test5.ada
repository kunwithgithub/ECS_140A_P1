procedure Test is

             A,B,C: constant := 30;
             E: constant := 20;
              
             K,I : INTEGER;
             
             begin
               if A > E and B = C then
                 K := 10;
               elsif B > C or E > A then
                 I := 30;
               else
                 K := 20;
               end if;
end Test;
