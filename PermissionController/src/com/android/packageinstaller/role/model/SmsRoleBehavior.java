/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.packageinstaller.role.model;

import android.content.Context;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.packageinstaller.permission.utils.CollectionUtils;

import java.util.List;

/**
 * Class for behavior of the SMS role.
 *
 * @see com.android.settings.applications.DefaultAppSettings
 * @see com.android.settings.applications.defaultapps.DefaultSmsPreferenceController
 * @see com.android.settings.applications.defaultapps.DefaultSmsPicker
 *
 */
public class SmsRoleBehavior implements RoleBehavior {

    @Override
    public boolean isAvailableAsUser(@NonNull Role role, @NonNull UserHandle user,
            @NonNull Context context) {
        UserManager userManager = context.getSystemService(UserManager.class);
        if (userManager.isManagedProfile(user.getIdentifier())) {
            return false;
        }
        // FIXME: STOPSHIP: Add an appropriate @SystemApi for this.
        //if (userManager.getUserInfo(user.getIdentifier()).isRestricted()) {
        //    return false;
        //}
        TelephonyManager telephonyManager = context.getSystemService(TelephonyManager.class);
        if (!telephonyManager.isSmsCapable()) {
            return false;
        }
        return true;
    }

    @Nullable
    @Override
    public String getFallbackHolder(@NonNull Role role, @NonNull Context context) {
        String defaultPackageName = ExclusiveDefaultHolderMixin.getDefaultHolder(role, context);
        if (defaultPackageName != null) {
            return defaultPackageName;
        }

        // TODO: STOPSHIP: This was the previous behavior, however this allows any third-party app
        // to suddenly become the default SMS app and get the permissions.
        List<String> qualifyingPackageNames = role.getQualifyingPackagesAsUser(
                Process.myUserHandle(), context);
        return CollectionUtils.firstOrNull(qualifyingPackageNames);
    }

    @Nullable
    @Override
    public CharSequence getConfirmationMessage(@NonNull Role role, @NonNull String packageName,
            @NonNull Context context) {
        return EncryptionUnawareConfirmationMixin.getConfirmationMessage(role, packageName,
                context);
    }
}
