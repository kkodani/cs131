%Kyle Kodani
%hw7
%with help from: piazza, http://www.gprolog.org/manual/gprolog.html, www.wikipedia.org

%Problem 1
put(K,V,[],[[K,V]]).
put(K,V,[[K,_]|T],[[K,V]|T]).
put(K,V,[[H,X]|T],[[H,X]|D]):-put(K,V,T,D), K\==H.

get(K,[[K,V]|_],V).
get(K,[_|T],V):-get(K,T,V).


%Problem 2
subseq([],_).
subseq([H|T1],[H|T2]):-subseq(T1,T2).
subseq([H1|T1],[_|T2]):-subseq([H1|T1],T2).


%Problem 3

%make sure a given set of 4 numbers has 1,2,3,4
permTest([A,B,C,D]):-permutation([A,B,C,D],[1,2,3,4]).

%try all rows, cols, and 2x2 squares with permTest
sudoku([[A1,A2,A3,A4],[B1,B2,B3,B4],[C1,C2,C3,C4],[D1,D2,D3,D4]],
       [[A1,A2,A3,A4],[B1,B2,B3,B4],[C1,C2,C3,C4],[D1,D2,D3,D4]]):-
	permTest([A1,A2,A3,A4]), permTest([A1,B1,C1,D1]), permTest([A1,A2,B1,B2]),
	permTest([B1,B2,B3,B4]), permTest([A2,B2,C2,D2]), permTest([A3,A4,B3,B4]),
	permTest([C1,C2,C3,C4]), permTest([A3,B3,C3,D3]), permTest([C1,C2,D1,D2]),
	permTest([D1,D2,D3,D4]), permTest([A4,B4,C4,D4]), permTest([C3,C4,D3,D4]).


%Problem 4

%check that assigned digits are legal
legalDigit(H):-member(H,[0,1,2,3,4,5,6,7,8,9]).
legalFirst([H|_]):-member(H,[1,2,3,4,5,6,7,8,9]).

%assign digits to letters
correspond([H]):-legalDigit(H).         
correspond([H|T]):-legalDigit(H), correspond(T), fd_all_different([H|T]).

%helper function to take the argument list and that list without the last element.
withoutLast([_],[]).
withoutLast([H|T],[H|T2]):-withoutLast(T,T2).

%convert list of nums to single integer
convert([Last],Last):-legalDigit(Last).
convert(L,WordNum):-last(L,LastElt), withoutLast(L,Elements),
		    convert(Elements,WordNum2), WordNum is (WordNum2*10+LastElt).

%check assignement, check first letters, convert words to numbers, check sum
verbalArithmetic(Letters,W1,W2,W3):-
	correspond(Letters), legalFirst(W1), legalFirst(W2),
	convert(W1,N1),convert(W2,N2), convert(W3,N3),
	WordSum is N1+N2, N3 = WordSum.


%Problem 5

%the six possible moves
move([[H|T],P2,P3], [T,[H|P2],P3], to(peg1,peg2)):-
	legalPeg([H|P2]).
move([[H|T],P2,P3], [T,P2,[H|P3]], to(peg1,peg3)):-
	legalPeg([H|P3]).
move([P1,[H|T],P3], [[H|P1],T,P3], to(peg2,peg1)):-
	legalPeg([H|P1]).
move([P1,[H|T],P3], [P1,T,[H|P3]], to(peg2,peg3)):-
	legalPeg([H|P3]).
move([P1,P2,[H|T]], [[H|P1],P2,T], to(peg3,peg1)):-
	legalPeg([H|P1]).
move([P1,P2,[H|T]], [P1,[H|P2],T], to(peg3,peg2)):-
	legalPeg([H|P2]).

%make sure peg after move is legal
legalPeg([_]).
legalPeg([Top, Second|_]):-(Top<Second).

%try moves until you reach goal
towerOfHanoi(Goal, Goal, []).
towerOfHanoi(Init, Goal, [NextMove|Rest]):-
	move(Init, NextState, NextMove),
	towerOfHanoi(NextState, Goal, Rest).
