
(* EXCEPTIONS *)

(* This is a marker for places in the code that you have to fill in.
   Your completed assignment should never raise this exception. *)
exception ImplementMe of string

(* This exception is thrown when a type error occurs during evaluation
   (e.g., attempting to invoke something that's not a function).
*)
exception DynamicTypeError

(* This exception is thrown when pattern matching fails during evaluation. *)  
exception MatchFailure  

(* EVALUATION *)

(* See if a value matches a given pattern.  If there is a match, return
   an environment for any name bindings in the pattern.  If there is not
   a match, raise the MatchFailure exception.
*)
let rec patMatch (pat:mopat) (value:movalue) : moenv =
  match (pat, value) with
      (* an integer pattern matches an integer only when they are the same constant;
	 no variables are declared in the pattern so the returned environment is empty *)
      (IntPat(i), IntVal(j)) when i=j -> Env.empty_env()
    | (BoolPat(bp), BoolVal(bv)) when bp=bv -> Env.empty_env()
    | (WildcardPat,_) -> Env.empty_env()
    | (VarPat(s), x) -> (Env.add_binding s x (Env.empty_env()))
    | (NilPat,ListVal(NilVal)) -> Env.empty_env()
    | (ConsPat(p1,p2), x) -> Env.combine_envs (patMatch p1 x) (patMatch p2 x)
    | _ -> raise MatchFailure
   (* | _ -> raise (ImplementMe "pattern matching not implemented")*)

    
(* Evaluate an expression in the given environment and return the
   associated value.  Raise a MatchFailure if pattern matching fails.
   Raise a DynamicTypeError if any other kind of error occurs (e.g.,
   trying to add a boolean to an integer) which prevents evaluation
   from continuing.
*)

(*Note: couldn't get Match to work*)
let rec evalExpr (e:moexpr) (env:moenv) : movalue =
  match e with
      (* an integer constant evaluates to itself *)
      IntConst(i) -> IntVal(i)
    | BoolConst(b) -> BoolVal(b)
    | Nil -> ListVal(NilVal)
    | Var(s) -> (try (Env.lookup s env) with
			Env.NotBound -> raise DynamicTypeError)
    | BinOp(exp1,op,exp2) -> let e1 = evalExpr exp1 env in
			     let e2 = evalExpr exp2 env in
				(match op with Plus -> (match (e1,e2) with
							(IntVal(i1),IntVal(i2)) -> IntVal(i1+i2)
							| _ -> raise DynamicTypeError
						       )
				
					   | Minus -> (match (e1,e2) with
                                                        (IntVal(i1),IntVal(i2)) -> IntVal(i1-i2)
							| _ -> raise DynamicTypeError
                                                      )

					   | Times -> (match (e1,e2) with
                                                        (IntVal(i1),IntVal(i2)) -> IntVal(i1*i2)
                                                        | _ -> raise DynamicTypeError
						      )

					   | Eq -> (match (e1,e2) with
                                                        (IntVal(i1),IntVal(i2)) -> BoolVal(i1=i2)
                                                        | _ -> raise DynamicTypeError
						   )

					   | Gt -> (match (e1,e2) with
                                                        (IntVal(i1),IntVal(i2)) -> BoolVal(i1>i2)
                                                        | _ -> raise DynamicTypeError
						   )

					   | Cons -> (match e2 with
							ListVal(l) -> ListVal(ConsVal(e1,l))
							| _ -> raise DynamicTypeError
						     )
				)
    | If(ifexp,thenexp,elseexp) -> (match (evalExpr ifexp env) with
					BoolVal(b) -> if (b)
						      then (evalExpr thenexp env)
						      else (evalExpr elseexp env)
					| _ -> raise DynamicTypeError
				   )
    | Function(pat,exp) -> FunctionVal(None,pat,exp,env)
    | FunctionCall(exp1,exp2) -> let f = (evalExpr exp1 env) in
				 let para = (evalExpr exp2 env) in
				 (match f with
					FunctionVal(None,fpat,fexp,fenv) -> 
								(evalExpr fexp (Env.combine_envs fenv (patMatch fpat para)))
					| _ -> raise DynamicTypeError)
   (* | Match(exp1,((lpat,lexp)::t)) -> (evalExpr exp1 (Env.combine_envs env (patMatch lpat exp1)))
					(evalExpr Match(exp1,t))*)
    | _ -> raise (ImplementMe "expression evaluation not implemented")


(* Evaluate a declaration in the given environment.  Evaluation
   returns the name of the variable declared (if any) by the
   declaration along with the value of the declaration's top-level expression.
*)

(*Note: I don't think LetRec works properly*)
let rec evalDecl (d:modecl) (env:moenv) : moresult =
  match d with
      Expr(e) -> (None, evalExpr e env)
    | Let(s,exp) -> ((Some s),(evalExpr exp env))
    | LetRec(s,pat,exp) -> ((Some s),(evalExpr exp (Env.combine_envs env (patMatch pat (evalExpr exp env)))))
    | _ -> raise (ImplementMe "let and let rec not implemented")

