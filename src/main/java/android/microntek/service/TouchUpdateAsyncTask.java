package android.microntek.service;

import android.os.*;
import android.content.*;
import java.io.*;
import android.widget.*;

public class TouchUpdateAsyncTask extends AsyncTask
{
  private Context mContext;
  private String mPath;

  TouchUpdateAsyncTask(final Context mContext, final String mPath) {
    this.mContext = mContext;
    this.mPath = mPath;
  }

  private int saveConfigPath(final byte[] fileContents, final String fileName) {
    final File file = new File(fileName);

    try {
      OutputStream output = new FileOutputStream(file);
      output.write(fileContents);
      output.flush();
      output.close();
    } catch (FileNotFoundException e) {
       return -1;
    } catch (IOException e) {
        return -1;
    }

    return 0;
  }

  private int touchCfg(final Context context, final String fileName) {
    final File file = new File(fileName);

    if (file.isFile() && file.exists()) {
      final StringBuilder fileContents = new StringBuilder();

      try {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        while (true) {
          final String line = bufferedReader.readLine();
          if (line == null) {
            continue;
          }
          fileContents.append(line);
        }
      } catch (UnsupportedEncodingException ex) {
        ex.printStackTrace();
      } catch (FileNotFoundException ex2) {
        ex2.printStackTrace();
      } catch (IOException ex3) {
        ex3.printStackTrace();
      }

      String[] tempArray = fileContents.toString().split(",");
      byte[] fileContentsByte = new byte[tempArray.length];
      int n2;
      int n3;
      int i = 0;
      for (String temp : tempArray) {
        final char char1 = temp.toUpperCase().charAt(2);
        final char char2 = temp.toUpperCase().charAt(3);
        if (char1 >= 'A' && char1 <= 'F') {
          n2 = char1 - 65 + 10;
        } else {
          n2 = char1 - 48;
        }
        if (char2 >= 'A' && char2 <= 'F') {
          n3 = char2 - 65 + 10;
        } else {
          n3 = char2 - 48;
        }

        fileContentsByte[i] = (byte) (n2 * 16 + n3);
        i++;
      }
     return saveConfigPath(fileContentsByte, "/proc/gt9xx_config");

    }
    return -1;
  }

    @Override
    protected Object doInBackground(Object[] params) {
    int touchCfg = -1;
    if (this.mContext != null && this.mPath != null) {
      touchCfg = this.touchCfg(this.mContext, this.mPath);
    }
    return touchCfg;
  }

  protected void onPostExecute(final Integer n) {
    final int n2 = 5000;
    if (n == 0) {
      Toast.makeText(this.mContext, (CharSequence)"update touch config sucess!!!", Toast.LENGTH_SHORT).show();
    }
    else {
      Toast.makeText(this.mContext, (CharSequence)"update touch config fail!!!", Toast.LENGTH_LONG).show();
    }
  }



    protected void onPreExecute() {
  }




  protected void onProgressUpdate(final Integer... array) {
  }
}
