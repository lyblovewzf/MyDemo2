package com.fighter_lee.filepicker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.internal.util.Predicate;
import com.fighter_lee.filepicker.data.Document;
import com.fighter_lee.filepicker.data.FileType;
import com.fighter_lee.filepicker.R;
import com.fighter_lee.filepicker.adapter.SectionsPagerAdapter;
import com.fighter_lee.filepicker.engine.PickerManager;
import com.fighter_lee.filepicker.inter.FileResultCallback;
import com.fighter_lee.filepicker.utils.MediaStoreHelper;
import com.fighter_lee.filepicker.utils.TabLayoutHelper;
import com.fighter_lee.filepicker.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class DocPickerFragment extends Fragment {

    private static final String TAG = DocPickerFragment.class.getSimpleName();

    TabLayout tabLayout;

    ViewPager viewPager;
    private ArrayList<String> selectedPaths;
    private ProgressBar progressBar;
    private DocPickerFragmentListener mListener;

    public DocPickerFragment() {
        // Required empty public constructor
    }

    public interface DocPickerFragmentListener{
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_doc_picker, container, false);
    }

    public static DocPickerFragment newInstance(ArrayList<String> selectedPaths) {
        DocPickerFragment docPickerFragment = new DocPickerFragment();
        docPickerFragment.selectedPaths = selectedPaths;
        return  docPickerFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DocPickerFragmentListener) {
            mListener = (DocPickerFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement DocPickerFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setViews(view);
        initView();
    }

    private void initView() {
        setUpViewPager();
        setData();
    }

    private void setViews(View view) {
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    private void setData() {
        MediaStoreHelper.getDocs(getActivity(), new FileResultCallback<Document>() {
            @Override
            public void onResultCallback(List<Document> files) {
                if(!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                setDataOnFragments(files);
            }
        });
    }

    private void setDataOnFragments(List<Document> files) {
        SectionsPagerAdapter sectionsPagerAdapter = (SectionsPagerAdapter) viewPager.getAdapter();
        if(sectionsPagerAdapter!=null)
        {
            for (int index = 0; index < sectionsPagerAdapter.getCount(); index++) {
                DocFragment docFragment = (DocFragment) getChildFragmentManager()
                        .findFragmentByTag(
                                "android:switcher:" + R.id.viewPager + ":"+index);
                if(docFragment!=null)
                {
                    FileType fileType = docFragment.getFileType();
                    if(fileType!=null)
                        docFragment.updateList(filterDocuments(fileType.extensions, files));
                }
            }
        }
    }

    private void setUpViewPager() {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getChildFragmentManager());
        ArrayList<FileType> supportedTypes = PickerManager.getInstance().getFileTypes();
        for (int index = 0; index < supportedTypes.size(); index++) {
            adapter.addFragment(DocFragment.newInstance(supportedTypes.get(index)),supportedTypes.get(index).title);
        }

        viewPager.setOffscreenPageLimit(supportedTypes.size());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        TabLayoutHelper mTabLayoutHelper = new TabLayoutHelper(tabLayout, viewPager);
        mTabLayoutHelper.setAutoAdjustTabModeEnabled(true);
    }

    private ArrayList<Document> filterDocuments(final String[] type, List<Document> documents)
    {
        final Predicate<Document> docType = new Predicate<Document>() {
            public boolean apply(Document document) {
                return document.isThisType(type);
            }
        };

        return new ArrayList<>(Utils.filter(new HashSet<>(documents),docType));
    }
}
