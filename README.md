<p>This application redirects the STDIN & STDOUT of C code segment running in JNI.</p>

<p>It creates 2 named pipes (FIFO), one for input and other for output.</p>

<p>It opens up one end of the pipe in write only mode in native code and other end of the pipe in read only mode in Java code. 
The file descriptor in native code is mapped to STDOUT i.e. 1, thereafter any writes to STDOUT in native code, will be 
redirected to other end of pipe which can read in Java code.</p>

<p> It opens up one end of the pipe in read only mode in native code and other end of the pipe in write only mode in Java code. 
The file descriptor in native code is mapped to STDIN i.e. 0, thereafter any writes to other end of pipe in Java code, will be
read by native code using STDIN.</p>
