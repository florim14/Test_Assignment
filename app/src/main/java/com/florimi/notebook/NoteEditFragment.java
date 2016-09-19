package com.florimi.notebook;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;


public class NoteEditFragment extends Fragment {


    private ImageButton noteCatButton;
    private Note.Category saveButtonCategory;
    private AlertDialog categoryDialogObject, confirmDialogObject;
    private EditText title;
    private EditText message;

    private Button btn;

    private boolean newNote  = false;

    private static final  String ModifiedCategory = "Modified Category";

    private long noteId = 0;

    public NoteEditFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //grab the bundle that sends along whether or note our noteEditFragment
        //is creating a new note
        Bundle bundle = this.getArguments();
        if(bundle != null)
        {
            newNote = bundle.getBoolean(NoteDetailActivity.NEW_NOTE_EXTRA, false);
        }

        //if we change the orientation we must look if any changes are made
        if(savedInstanceState != null)
        {
            saveButtonCategory = (Note.Category) savedInstanceState.get(ModifiedCategory);
        }
        //inflate our fragment edit layout
        View fragmentLayout = inflater.inflate(R.layout.fragment_note_edit, container, false);

        btn = (Button) fragmentLayout.findViewById(R.id.btnWeb);

        //grab widgets references from layout
        title = (EditText) fragmentLayout.findViewById(R.id.editNoteTitle);
        message = (EditText) fragmentLayout.findViewById(R.id.editNoteMessage);
        noteCatButton = (ImageButton) fragmentLayout.findViewById(R.id.editNoteButton);
        Button savedButton = (Button) fragmentLayout.findViewById(R.id.saveNote);

        //populate widgets with note data
        Intent intent = getActivity().getIntent();
        title.setText(intent.getExtras().getString(MainActivity.NOTE_TITLE_EXTRA, ""));
        message.setText(intent.getExtras().getString(MainActivity.NOTE_MESSAGE_EXTRA, ""));
        noteId = intent.getExtras().getLong(MainActivity.NOTE_ID_EXTRA, 0);

        //if we grab a category from our bundle that we know we changed the orientation and saved information
        //so set out image button background to that category
        if(saveButtonCategory != null)
        {
            noteCatButton.setImageResource(Note.categoryToDrawable(saveButtonCategory));
        }
        //otherwise we came from our list fragment so just do everything normally
         else if(!newNote){
            Note.Category noteCat = (Note.Category) intent.getSerializableExtra(MainActivity.NOTE_CATEGORY_EXTRA);
            saveButtonCategory = noteCat;
            noteCatButton.setImageResource(Note.categoryToDrawable(noteCat));
        }
        buildCategoryDialog();
        buildConfirmDialog();

        savedButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void  onClick(View v)
            {
                confirmDialogObject.show();
            }
        });

        noteCatButton.setOnClickListener(new View.OnClickListener(){
             @Override
            public  void  onClick(View v)
             {
                 categoryDialogObject.show();
             }
        });

        return fragmentLayout;
    }

    @Override
    public void onSaveInstanceState (Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putSerializable(ModifiedCategory, saveButtonCategory);

    }

    private void buildCategoryDialog() {

        final String[] categories = new String[]{"Personal", "Android", "iPhone", "Windows" };
        AlertDialog.Builder categoryBuilder = new AlertDialog.Builder(getActivity());
        categoryBuilder.setTitle("Choose Note Type!");

        categoryBuilder.setSingleChoiceItems(categories, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int meunuitem) {
                //dismisses our dialog window
                categoryDialogObject.cancel();

                switch (meunuitem) {
                    case 0:
                        saveButtonCategory = Note.Category.PERSONAL;
                        noteCatButton.setImageResource(R.drawable.p);
                        btn.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View view){
                                Intent browserIntent =
                                        new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                                startActivity(browserIntent);
                            }
                        });
                        break;
                    case 1:
                        saveButtonCategory = Note.Category.ANDROID;
                        noteCatButton.setImageResource(R.drawable.t);
                        btn.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View view){
                                Intent browserIntent =
                                        new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.android.com"));
                                startActivity(browserIntent);
                            }
                        });
                        break;
                    case 2:
                        saveButtonCategory = Note.Category.iPHONE;
                        noteCatButton.setImageResource(R.drawable.q);
                        btn.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View view){
                                Intent browserIntent =
                                        new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.apple.com/iphone"));
                                startActivity(browserIntent);
                            }
                        });
                        break;
                    case 3:
                        saveButtonCategory = Note.Category.WINDOWS;
                        noteCatButton.setImageResource(R.drawable.f);
                        btn.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View view){
                                Intent browserIntent =
                                        new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.microsoft.com/en-us/windows/"));
                                startActivity(browserIntent);
                            }
                        });
                        break;
                }
            }
        });

        categoryDialogObject = categoryBuilder.create();
    }

    private void buildConfirmDialog() {
        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(getActivity());

        confirmBuilder.setTitle("Are you sure?");
        confirmBuilder.setMessage("Are you sure, you want to save the note?");

        confirmBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which){
                Log.d("Save Note", "Note title: "+title.getText()+" note message: " + message.getText() + " note category: "+
                saveButtonCategory);

                NotebookDbAdapter dbAdapter = new NotebookDbAdapter(getActivity().getBaseContext());
                dbAdapter.open();

                //if it is a new note create to our database
                if (newNote)
                {
                    dbAdapter.createNote(title.getText() + "", message.getText() + "",
                            (saveButtonCategory == null)?Note.Category.PERSONAL : saveButtonCategory);
                }
                else {
                    //otherwise it's a old note so update ini our database
                    dbAdapter.updateNote( noteId, title.getText() + "", message.getText() + "", saveButtonCategory);
                }
                dbAdapter.close();
                Intent intent = new Intent(getActivity(),MainActivity.class);
                startActivity(intent);

            }
        });

        confirmBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                //do nothing here
            }
        });
        confirmDialogObject = confirmBuilder.create();
    }

}
