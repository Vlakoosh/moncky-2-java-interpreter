
## Installation/usage

Download the project as a zip and extract all contents.

## How to run
User can either compile assembly code into a hex file that can be read by the moncky-2 processor, or run a java-based simulation of the processor.

#### compiler
- place "code.txt" in "moncky2in" folder with assembly source code
- run "Moncky2Compiler.java"
- a new file will be created in "moncky2out" directory under the name "compiledCode.hex"
- this .hex file can be put into the memory of the moncky-2 processor, either physical or digital, and ran (link to digital files below)

#### simulation/interpreter
- place "code.txt" in "moncky2in" folder with assembly source code
- run "Moncky2Interpreter.java"
- a new console window will appear and print all final values in the registers and memory

#### linter/syntax checker
- place "code.txt" in "moncky2in" folder with assembly source code
- run "Moncky2Linter.java"
- a new console window will appear and print all errors and warnings related to your code. Use for debugging!


## Acknowledgements

 - [Digital Files for Moncky-2 processor](https://gitlab.com/big-bat/moncky/-/tree/master/Moncky2_digital)
 - [Software to run digital processor](https://github.com/hneemann/Digital)



## Inspiration

- [Kris Demuynck - creator of my infrastructure 2 course book](https://www.linkedin.com/in/krisdemuynck/)

