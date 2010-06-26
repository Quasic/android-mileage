package com.evancharlton.mileage;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.evancharlton.mileage.io.CsvImportActivity;
import com.evancharlton.mileage.io.DbImportActivity;
import com.evancharlton.mileage.provider.Settings;

public class ImportActivity extends Activity {
	private static final String FILENAME = "filename";

	private static final String[] FILE_TYPES = new String[] {
			".db",
			".csv"
	};

	@SuppressWarnings("unchecked")
	private static final Class[] EXPORTERS = new Class[] {
			CsvImportActivity.class,
			DbImportActivity.class
	};

	private FileLoader mFileLoader;
	private Spinner mFileTypes;
	private Spinner mInputFile;
	private Button mSubmitButton;
	private FileAdapter mFileAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.import_form);

		mFileAdapter = new FileAdapter(this);

		mFileTypes = (Spinner) findViewById(R.id.exporter);
		mInputFile = (Spinner) findViewById(R.id.files);
		mSubmitButton = (Button) findViewById(R.id.submit);
		mSubmitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ImportActivity.this, EXPORTERS[mFileTypes.getSelectedItemPosition()]);
				intent.putExtra(ImportActivity.FILENAME, getFilename());
				startActivity(intent);
			}
		});

		mInputFile.setAdapter(mFileAdapter);

		mFileLoader = (FileLoader) getLastNonConfigurationInstance();

		mFileTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (mFileLoader == null) {
					mFileLoader = new FileLoader();
				}
				mFileLoader.attach(ImportActivity.this);
				if (mFileLoader.getStatus() == AsyncTask.Status.PENDING) {
					mFileLoader.execute();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	private FilenameFilter getFilter() {
		return new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				final String extension = FILE_TYPES[mFileTypes.getSelectedItemPosition()];
				Log.d("FilenameFilter", filename + " ~= " + extension);
				return filename.toLowerCase().endsWith(extension);
			}
		};
	}

	private void setFiles(String[] filenames) {
		mFileAdapter.setData(filenames);
		mFileLoader = null;
		mSubmitButton.setEnabled(!mFileAdapter.isEmpty());
	}

	private String getFilename() {
		return mFileAdapter.getItem(mInputFile.getSelectedItemPosition()).toString();
	}

	private static class FileLoader extends AsyncTask<Void, Void, String[]> {
		private ImportActivity mActivity;

		public void attach(ImportActivity activity) {
			mActivity = activity;
		}

		@Override
		protected String[] doInBackground(Void... voids) {
			return new File(Settings.EXTERNAL_DIR).list(mActivity.getFilter());
		}

		@Override
		protected void onPostExecute(String[] files) {
			mActivity.setFiles(files);
		}
	}

	private static class FileAdapter extends BaseAdapter implements SpinnerAdapter {
		private final LayoutInflater mInflater;
		private String[] values = new String[0];

		public FileAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return values.length;
		}

		@Override
		public Object getItem(int position) {
			return values[position];
		}

		@Override
		public long getItemId(int position) {
			return values[position].hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(android.R.layout.simple_spinner_item, parent, false);
			}

			TextView tv = (TextView) convertView.getTag();
			if (tv == null) {
				tv = (TextView) convertView.findViewById(android.R.id.text1);
				convertView.setTag(tv);
			}
			tv.setText(values[position]);

			return convertView;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
			}

			CheckedTextView ctv = (CheckedTextView) convertView.getTag();
			if (ctv == null) {
				ctv = (CheckedTextView) convertView.findViewById(android.R.id.text1);
				convertView.setTag(ctv);
			}
			ctv.setText(values[position]);

			return convertView;
		}

		public void setData(String[] data) {
			values = data;
			notifyDataSetChanged();
		}
	}
}