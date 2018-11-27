package com.calangoni.platcplmob_t02.feature

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import kotlinx.android.synthetic.main.fragment_main.*
import java.io.File
import java.io.InputStream
import java.net.URL

/**
 * A placeholder fragment containing a simple view.
 */
class MainActivityFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainView = inflater.inflate(R.layout.fragment_main, container, false)

        // get reference to button
        val btn_motionPhoto = mainView.findViewById(R.id.btn_motionPhoto) as Button
        // set on-click listener
        btn_motionPhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this.context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // your code to perform when the user clicks on the button
                // Toast.makeText(this@MainActivity, "You clicked me.", Toast.LENGTH_SHORT).show()
                Log.i("PlatCPL", "Chamando verificarFotos()")
                MyAsyncTask().execute("/sdcard/DCIM/Camera")
            }
        }

        return mainView
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun procurarSepararVideo(pOrigem: String, pDestino: String): String

    fun verificarFotos(path: String = "/sdcard/DCIM/Camera") {
        Log.i("PlatCPL", "Dentro de verificarFotos: " + path)
        val lista = File(path).listFiles()
        Log.i("PlatCPL", "lista == null ? => " + (lista == null))
        lista.forEach { it: File ->
            if (it.isFile && it.extension.toLowerCase() == "jpg") {
                val inPath = path + "/" + it.nameWithoutExtension + ".jpg"
                val outPath = "/sdcard/DCIM/Motion/" + it.nameWithoutExtension + ".mp4"
                if (!File(outPath).exists()) {
                    val retorno = procurarSepararVideo(inPath, outPath)
                    Log.i("PlatCPL", retorno)
                }
            }
        }
    }

    fun encontrarMotionPhoto(pathIn: String, pathOut: String) {
        val myFile = File(pathIn)
        var ins: InputStream = myFile.inputStream()
        val size: Long = myFile.length()
        var iSuc: Int = 0
        var iFile: Long = 0
        val magica = "MotionPhoto_Data"
        while(iFile < size) {
            if (ins.read() === magica[iSuc].toInt()) {
                iSuc++
                if (iSuc === magica.length) {
                    // ???({ std.cout() }) shl "Encontrado MotionPhoto_Data !! " shl (iFile-magica.length()) shl ???({ std.endl() })
                    writeSplittedVideo(pathOut, ins)
                    return
                } else
                    continue
            } else
                iSuc = 0
        }
    }

    fun writeSplittedVideo(pathOut: String, ins: InputStream) {
        val outfile = File(pathOut)
        outfile.createNewFile()
        val ous = outfile.outputStream()
        val buffer = ByteArray(500000)
        var readBytes = ins.read(buffer)
        while (readBytes > 0) {
            ous.write(buffer, 0, readBytes)
            readBytes = ins.read(buffer)
        }
        ous.close()
    }

    // AsyncTask inner class
    inner class MyAsyncTask : AsyncTask<String, String, String>() {

        private var result: String = ""

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg pathArg: String): String {
            val path: String = pathArg[0]
            val lista = File(path).listFiles()
            var pasta = File("/sdcard/DCIM/Motion")
            if (!pasta.exists()) {
                pasta.mkdir()
            }
            lista.forEach { it: File ->
                if (it.isFile && it.extension.toLowerCase() == "jpg") {
                    val inPath = path + "/" + it.nameWithoutExtension + ".jpg"
                    val outPath = "/sdcard/DCIM/Motion/" + it.nameWithoutExtension + ".mp4"
                    if (!File(outPath).exists()) {
                        val retorno = procurarSepararVideo(inPath, outPath)
                        Log.i("PlatCPL", retorno)
                        publishProgress(retorno)
                    }
                }
            }
            return "Concluído!"
        }

        override fun onProgressUpdate(vararg values: String?) {
            super.onProgressUpdate(*values)
            sample_text.text = values[0]
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            //set result in textView
            sample_text.text = result
        }
    }
}
