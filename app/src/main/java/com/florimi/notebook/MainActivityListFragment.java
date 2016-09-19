package com.florimi.notebook;


import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivityListFragment extends ListFragment {

    private ArrayList<Note> notes;
    private NoteAdapter noteAdapter;


    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);


        NotebookDbAdapter  dbAdapter = new NotebookDbAdapter(getActivity().getBaseContext());
        dbAdapter.open();
        notes = dbAdapter.getAllNotes();
        dbAdapter.close();

        noteAdapter=new NoteAdapter(getActivity(), notes);
        setListAdapter(noteAdapter);

        getListView().setDivider(ContextCompat.getDrawable(getActivity(),android.R.color.black));
        getListView().setDividerHeight(1);

        registerForContextMenu(getListView());

    }

    @Override
    public void  onListItemClick(ListView l,View v, int position, long id){
        super.onListItemClick(l,v,position,id);
        launchNoteDetailActivity(MainActivity.FragmentToLaunch.View,position);
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem){

        //give me position of whatever note i long pressed on
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
        int rowPosition = info.position;
        Note note = (Note) getListAdapter().getItem(rowPosition);

        //returns to us the id of whatever item we selected
        switch (menuItem.getItemId()){
            //if we press edit
            case R.id.edit:
                //do something here
                launchNoteDetailActivity(MainActivity.FragmentToLaunch.Edit, rowPosition);
                Log.d("Menu Clicks.","We pressed Edit.");
                return  true;
            case R.id.delete:
                NotebookDbAdapter dbAdapter = new NotebookDbAdapter(getActivity().getBaseContext());
                dbAdapter.open();
                dbAdapter.deleteNote(note.getId());

                notes.clear();
                notes.addAll(dbAdapter.getAllNotes());
                noteAdapter.notifyDataSetChanged();

                dbAdapter.close();
        }

        return  super.onContextItemSelected(menuItem);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu,v,menuInfo);

        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.long_press_menu, menu);
    }

    private  void launchNoteDetailActivity(MainActivity.FragmentToLaunch ftl, int position){

        //grap the note information associated with whatever note item we clicked on

        Note note = (Note) getListAdapter().getItem(position);

        //create a new intent that launches our note detail activity
        Intent intent = new Intent(getActivity(), NoteDetailActivity.class);

        //pass along the information of the note we clicked on to our note detail activity
        intent.putExtra(MainActivity.NOTE_TITLE_EXTRA,note.getTitle());
        intent.putExtra(MainActivity.NOTE_MESSAGE_EXTRA,note.getMessage());
        intent.putExtra(MainActivity.NOTE_CATEGORY_EXTRA, note.getCategory());
        intent.putExtra(MainActivity.NOTE_ID_EXTRA, note.getId());

        switch (ftl){
            case View:
                intent.putExtra(MainActivity.NOTE_FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.View);
                break;
            case Edit:
                intent.putExtra(MainActivity.NOTE_FRAGMENT_TO_LOAD_EXTRA, MainActivity.FragmentToLaunch.Edit);
                break;

        }
        startActivity(intent);
    }
}
