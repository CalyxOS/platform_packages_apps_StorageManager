/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.storagemanager.deletionhelper;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import androidx.preference.PreferenceViewHolder;
import com.android.storagemanager.R;
import com.android.storagemanager.deletionhelper.DeletionType.LoadingStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.LooperMode;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
@LooperMode(LEGACY)
public class CollapsibleCheckboxPreferenceGroupTest {

    private Context mContext;
    private PreferenceViewHolder mHolder;
    private CollapsibleCheckboxPreferenceGroup mPreference;
    @Mock private DeletionType mDeletionType;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mContext = RuntimeEnvironment.application;
        mPreference = new CollapsibleCheckboxPreferenceGroup(mContext, null);

        // Inflate the preference and the widget.
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final View view =
                inflater.inflate(
                        mPreference.getLayoutResource(), new LinearLayout(mContext), false);

        mHolder = PreferenceViewHolder.createInstanceForTests(view);
    }

    @Test
    public void testItemVisibilityBeforeLoaded() {
        mPreference.onBindViewHolder(mHolder);

        assertThat(mHolder.findViewById(R.id.progress_bar).getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(mHolder.findViewById(android.R.id.icon).getVisibility()).isEqualTo(View.GONE);
        assertThat(mHolder.findViewById(android.R.id.widget_frame).getVisibility())
                .isEqualTo(View.GONE);
    }

    @Test
    public void testItemVisibilityAfterLoaded() {
        when(mDeletionType.getLoadingStatus()).thenReturn(LoadingStatus.COMPLETE);
        mPreference.switchSpinnerToCheckboxOrDisablePreference(
                100L, mDeletionType.getLoadingStatus());
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        mPreference.onBindViewHolder(mHolder);

        // After onFreeableChanged is called, we're no longer loading.
        assertThat(mHolder.findViewById(R.id.progress_bar).getVisibility()).isEqualTo(View.GONE);
        assertThat(mHolder.findViewById(android.R.id.icon).getVisibility()).isEqualTo(View.GONE);
        assertThat(mHolder.findViewById(android.R.id.widget_frame).getVisibility())
                .isEqualTo(View.VISIBLE);
    }

    @Test
    public void loadCompleteUpdate_disablesWhenNothingToDelete() {
        when(mDeletionType.getLoadingStatus()).thenReturn(LoadingStatus.EMPTY);
        mPreference.switchSpinnerToCheckboxOrDisablePreference(0, mDeletionType.getLoadingStatus());
        mPreference.onBindViewHolder(mHolder);

        assertThat(mPreference.isEnabled()).isFalse();
    }

    @Test
    public void loadCompleteUpdate_enabledWhenDeletableContentFound() {
        when(mDeletionType.getLoadingStatus()).thenReturn(LoadingStatus.COMPLETE);
        mPreference.switchSpinnerToCheckboxOrDisablePreference(
                100L, mDeletionType.getLoadingStatus());
        mPreference.onBindViewHolder(mHolder);

        assertThat(mPreference.isEnabled()).isTrue();
    }
}
