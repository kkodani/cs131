
(* Name: Kyle Kodani

   UID: 903896275

   Others With Whom I Discussed Things:

   Other Resources I Consulted: http://caml.inria.fr/pub/docs/manual-ocaml/libref/List.html
   
*)



(* Problem 1a
   map2: ('a -> 'b -> 'c) -> 'a list -> 'b list -> 'c list
*)

let map2 f l1 l2 =
	match l1 with
		[] -> []
		| h1::t1 -> match l2 with
				[] -> []
				| h2::t2 -> (f h1 h2)::(map2 f t1 t2)

(* Problem 1b
   rev: 'a list -> 'a list
*)

let rev l =
	List.fold_right (fun x rl -> rl@[x]) l []

(* Problem 1c
   rev2: 'a list -> 'a list
*)

let rev2 l =
        List.fold_left (fun x rl -> rl::x) [] l


(* Problem 1d
   curry: ('a * 'b -> 'c) -> ('a -> 'b -> 'c)
   uncurry: ('a -> 'b -> 'c) -> ('a * 'b -> 'c)
*)

let curry f =
	(fun e1 e2 -> (f (e1,e2)))

let uncurry f =
	(fun (e1,e2) -> (f e1 e2))

(* Problem 1e
   mapAllPairs: ('a -> 'b -> 'c) -> 'a list -> 'b list -> 'c list
*)

let mapAllPairs f l1 l2 =
	List.concat(List.map (fun e1 -> List.map (fun e2 -> f e1 e2) l2) l1)

(* Dictionaries *)    

(* Problem 2a
   empty1: unit -> ('a * 'b) list
   put1: 'a -> 'b -> ('a * 'b) list -> ('a * 'b) list
   get1: 'a -> ('a * 'b) list -> 'b
*)  

let empty1 =
	[]

let put1 k v d =
	(k,v)::d

let rec get1 k d =
	match d with
		[] -> raise Not_found
		| (k',v')::t -> if k=k' then v'
				else (get1 k t)
	
	
(* Problem 2b
   empty2: unit -> ('a,'b) dict2
   put2: 'a -> 'b -> ('a,'b) dict2 -> ('a,'b) dict2
   get2: 'a -> ('a,'b) dict2 -> 'b
*)  
    
type ('a,'b) dict2 = Empty | Entry of 'a * 'b * ('a,'b) dict2

let empty2 =
	Empty

let put2 k v d =
	Entry (k,v,d)

let rec get2 k d =
	match d with
		Empty -> raise Not_found
		| Entry(k',v',d') -> if k=k' then v'
				else (get2 k d')

	
(* Problem 2c
   empty3: unit -> ('a,'b) dict3
   put3: 'a -> 'b -> ('a,'b) dict3 -> ('a,'b) dict3
   get3: 'a -> ('a,'b) dict3 -> 'b
*)  

type ('a,'b) dict3 = ('a -> 'b)

let empty3 =
	fun s -> raise Not_found

let put3 k v d =
	fun s -> if k=s then v else d s

let get3 k d =
	d k

(* Calculators *)    
  
(* A type for arithmetic expressions *)
  
type op = Plus | Minus | Times | Divide
type aexp = Num of float | BinOp of aexp * op * aexp

(* Problem 3a
   evalAExp: aexp -> float
*)

let operator o e1 e2 =
	match o with
		Plus -> e1+.e2
                | Minus -> e1-.e2
                | Times -> e1*.e2
                | Divide -> e1/.e2

let rec evalAExp e =
	match e with
		Num (n) -> n
		| BinOp (exp1,o,exp2) -> (operator o (evalAExp exp1) (evalAExp exp2))

(* A type for stack operations *)	  
	  
type sopn = Push of float | Swap | Calculate of op

(* Problem 3b
   evalRPN: sopn list -> float
*)

let evalRPN sl =
	let rec helperRPN sl fl = 
		match sl with
			[] -> (match fl with [h] -> h)
			| Push(n)::t -> (helperRPN t (n::fl))
			| Swap::t1 -> (match fl with
					f::s::t2 -> (helperRPN t1 (s::f::t2))
				      )
			| Calculate(o)::t1 -> match fl with
						f::s::t2 -> (helperRPN t1 ((operator o f s)::t2))
	in
	helperRPN sl []


(* Problem 3c
   toRPN: aexp -> sopn list
*)

let rec toRPN e =
	match e with
		Num(n) -> [Push(n)]
		| BinOp(exp1,o,exp2) -> (toRPN exp1)@(toRPN exp2)@[(Calculate(o))]
  
(* Problem 3d
   toRPNopt: aexp -> (sopn list * int)
*)


(*DOES NOT WORK*)
let rec toRPNopt e = 
	([],0)
