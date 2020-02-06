Procedure overload is 
  function max (x: integer; y: integer)
     return integer is
  begin
     if x > y then return x;
     else return y;
     end if;
  end max;
  
  function max(x: integer; y: integer)
     return float is
  begin
     if x > y then return float(x);
     else return float(y);
     end if;
  end max;

  a: integer;
  b: float;
  begin
     a := max(2,3);
     b := max(2,3);
end overload;
  
--reference from text book p284  
