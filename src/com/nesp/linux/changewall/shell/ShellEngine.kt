package com.nesp.linux.changewall.shell

import java.util.logging.Level.SEVERE
import java.io.IOException
import com.sun.xml.internal.ws.streaming.XMLStreamReaderUtil.close
import jdk.nashorn.internal.runtime.ScriptingFunctions.readLine
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.io.InputStreamReader
import java.io.BufferedReader


class ShellEngine {

    companion object {

        @JvmStatic
        @Throws(InterruptedException::class)
        fun exec(command: String): ExecResult? {
            var execResult: ExecResult? = null
            var returnString = ""
            var pro: Process?
            val runTime = Runtime.getRuntime()
            if (runTime == null) {
                System.err.println("Create runtime false!")
            }
            try {
                pro = runTime!!.exec(command)
                val input = BufferedReader(InputStreamReader(pro!!.inputStream))
                val output = PrintWriter(OutputStreamWriter(pro.outputStream))
                var line = input.readLine()
                while (line != null) {
                    returnString = returnString + input.readLine() + "\n"
                    line = input.readLine()
                }
                input.close()
                output.close()
                execResult?.apply {
                    isSuccess = pro.exitValue() == 0
                    exitValue = pro.exitValue()
                    msg = returnString
                }
                pro.destroy()
            } catch (ex: IOException) {
                execResult?.apply {
                    isSuccess = false
                    exitValue = 1
                    msg = returnString
                }
            }

            return execResult
        }
    }

    data class ExecResult(
            var isSuccess: Boolean,
            var exitValue: Int,
            var msg: String
    )
}