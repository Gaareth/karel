
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

void computeFibonacci() 
{
   
    moveForward();
    moveForward();
    
    let n = 8;
    
    let a = 1;
    let b = 1;
    repeat (n) {
        let temp = a + b;
        a = b;
        b = temp;
        
        let c = 0;
        turnRight();
        while (c != b) {
            fibonize();
            c = c + 1;
        }
        turnLeft();
        moveForward();
    }
    
   
