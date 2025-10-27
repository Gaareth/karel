# Karel Enhanced :^)
Adds support for Expressions, Variables, Function arguments and return values

Supports type checking, and two types:
- num: for integers
- bool: for boolean values

## Demon
Here is an example of the computedFibonacci exercise. Written in a convoluted and very inefficent way, but fitting to demonstrate functions and their variables.
<img width="722" height="976" alt="karel code solving computedFibonacci" src="https://github.com/user-attachments/assets/93aa7425-6a2b-477c-b439-e63dfc39169f" />



## Limitations (currently)
Fork of: https://github.com/fredoverflow/karel.
This is just a fun experiment, so this will probably not be updated and lag behind the original repo. Nevertheless, this should still be usable in its current state.

- no floating point numbers
- cant use functions as expressions before declaring them 
  - TODO: first parse all function declarations/headers to get their type
- bugs?
- Old [freditor](https://github.com/fredoverflow/karel) commit: 57c5da8e31411e566bd1ddae75ca9d29b9e44754, Nov 3, 2022

## Install
Just download karel.jar

## Build
See DEVELOP.md

**but use commit `57c5da8e31411e566bd1ddae75ca9d29b9e44754`**



## Example
```karel

void increment() {
    while (onBeeper()) {
        pickBeeper();
        moveForward();
    }
    dropBeeper();
}

void gotoWall() {
    while (frontIsClear()) {
        moveForward();
    }
}

void fibonize() {
    increment();
    turnAround();
    gotoWall();
    turnAround();
}

num fib(n: num) {
    let a = 0;
    let b = 1;
    repeat (n) {
        let temp = a + b;
        a = b;
        b = temp;
    }
    
    return b;
}

num add(a: num, b: num) {
    return a + b;
}

void computeFibonacci() 
{ 
    let n = add(4,4);
    // skip the first two and start and fib = 3
    let i = 3;
    moveForward();
    moveForward();
    
    repeat (n) {
        let fib_value = fib(i);
        
        let c = 0;
        turnRight();
        while (c != fib_value) {
            fibonize();
            c = c + 1;
        }
        turnLeft();
        i = i + 1;
        if (frontIsClear()) {
            moveForward();
        }
    }
}


```