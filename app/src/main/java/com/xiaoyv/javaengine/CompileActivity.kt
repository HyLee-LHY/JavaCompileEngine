package com.xiaoyv.javaengine

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.PathUtils
import com.google.googlejavaformat.java.Formatter
import com.google.googlejavaformat.java.JavaFormatterOptions
import com.xiaoyv.java.compiler.JavaEngine
import com.xiaoyv.javaengine.databinding.ActivitySingleSampleBinding
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


/**
 * CompileActivity
 *
 * @author why
 * @since 2022/3/9
 */
class CompileActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private lateinit var binding: ActivitySingleSampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySingleSampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initEvent()

        setOutputSample()
    }

    private fun initEvent() {
        binding.toolbar.menu.add("输入示例")
            .setOnMenuItemClickListener {
                setInputSample()
                true
            }.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)

        binding.toolbar.menu.add("输出示例")
            .setOnMenuItemClickListener {
                setOutputSample()
                true
            }.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)

        binding.toolbar.menu.add("Run")
            .setOnMenuItemClickListener {
                formatCode()
                runProgram()
                true
            }.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)


    }

    private fun formatCode() {
        val codeText = binding.codeText.text.toString()
        val formatSource = Formatter(JavaFormatterOptions.builder()
            .style(JavaFormatterOptions.Style.AOSP)
            .build())
            .formatSource(codeText)
        binding.codeText.setText(formatSource)
    }

    /**
     * [JavaEngine.CompileExceptionHandler] 为内部编译相关的协程作用域 默认异常捕获实现。
     *
     * - 你可以自定义获取整个流程抛出的异常信息
     */
    private fun runProgram() = launch(JavaEngine.CompileExceptionHandler) {
        binding.printView.text = null

        // build 文件夹
        val buildDir = PathUtils.getExternalAppFilesPath() + "/SingleExample/build"

        // java 文件夹
        val javaDir = PathUtils.getExternalAppFilesPath() + "/SingleExample/java"

        // 待编译的 Main.java
        val javaFilePath = withContext(Dispatchers.IO) {
            // java 文件夹内 Main.java 文件，写入代码内容
            val javaFilePath = "$javaDir/Main.java"

            FileIOUtils.writeFileFromString(javaFilePath, binding.codeText.text.toString())

            // 返回源文件路径
            javaFilePath
        }

        // 编译 class，libFolder 为第三方 jar 存放目录，没有传空即可
        // 编译完成返回目标 classes.jar，内部通过协程在 IO 线程处理的
        val compileJar: File = JavaEngine.classCompiler.compile(
            sourceFileOrDir = javaFilePath,
            buildDir = buildDir,
            libFolder = null
        ) { taskName, progress ->

            // 这里是进度，回调在主线程...
            binding.printView.append(String.format("%3d%% Compiling: %s\n", progress, taskName))
        }
        binding.printView.append("Compiling class success!\n\n")

        binding.printView.append("Compiling dex start...\n")

        // 编译 classes.dex，这一步相关的信息通过 System.xxx.print 输出
        val dexFile = JavaEngine.dexCompiler.compile(compileJar.absolutePath, buildDir)

        binding.printView.append("Compiling dex success!\n\n")

        binding.printView.append("Run dex start...\n\n")

        // JavaEngine.
        val programConsole = JavaEngine.javaProgram.run(dexFile, arrayOf("args"),
            chooseMainClassToRun = { classes, continuation ->
                val dialog = AlertDialog.Builder(this@CompileActivity)
                    .setTitle("请选择一个主函数运行")
                    .setItems(classes.toTypedArray()) { p0, p1 ->
                        p0.dismiss()
                        continuation.resume(classes[p1])
                    }
                    .setCancelable(false)
                    .setNegativeButton("取消") { d, v ->
                        d.dismiss()
                        continuation.resumeWithException(Exception("取消操作"))
                    }.create()

                dialog.show()
                dialog.setCanceledOnTouchOutside(false)
            },
            printOut = {
                binding.printView.append(it)
            },
            printErr = {
                binding.printView.append(Html.fromHtml("<font color=\"#FF0000\">$it</font>"))
            })

        binding.btSend.setOnClickListener {
            val input = binding.inputEdit.text.toString()
            programConsole.inputStdin(input)
            binding.inputEdit.text = null
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setOutputSample() {
        binding.codeText.setText(
            "/**\n" +
                    " * @author Admin\n" +
                    " */\n" +
                    "public class Main {\n" +
                    "\n" +
                    "    public static void main(String[] args) {\n" +
                    "        System.out.println(\"Start Thread!\");\n" +
                    "        new Thread(()-> System.err.println(\"Hello World!\")).start();\n" +
                    "    }\n" +
                    "}"
        )
    }

    @SuppressLint("SetTextI18n")
    private fun setInputSample() {
        binding.codeText.setText(
            "import java.util.Scanner;\n\n" +
                    "public class Main {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        System.out.println(\"Hello System.in\");\n" +
                    "        System.out.println(\"Please enter something\");\n" +
                    "        Scanner scanner = new Scanner(System.in);\n" +
                    "        String line = scanner.nextLine();\n" +
                    "        System.out.println(\"The following is your input:\");\n" +
                    "        System.out.println(line);\n" +
                    "    }\n" +
                    "}"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}