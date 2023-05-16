/*
 * Copyright (c) 2023
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.miniorange.twofactor.jenkins;

import com.miniorange.twofactor.jenkins.tfaMethodsAuth.MoSecurityQuestionAuth;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.RootAction;
import hudson.model.User;
import java.util.Objects;
import java.util.logging.Logger;
import jenkins.model.Jenkins;

@SuppressWarnings("unused")
@Extension
public class MoUserAuth implements RootAction, Describable<MoUserAuth> {

  private static final Logger LOGGER = Logger.getLogger(MoUserAuth.class.getName());

  public MoUserAuth() {}

  @Override
  public String getIconFileName() {
    return null;
  }

  @Override
  public String getDisplayName() {
    return "tfaUserAuth";
  }

  @Override
  public String getUrlName() {
    return "tfaUserAuth";
  }

  public String getUserId() {
    return User.current() != null ? Objects.requireNonNull(User.current()).getId() : "";
  }

  @SuppressWarnings("unused")
  public MoSecurityQuestionAuth getSecurityQuestionAuth() {
    return new MoSecurityQuestionAuth();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Descriptor<MoUserAuth> getDescriptor() {
    return Jenkins.get().getDescriptorOrDie(getClass());
  }

  @SuppressWarnings("unused")
  public static final MoUserAuth.DescriptorImpl DESCRIPTOR = new MoUserAuth.DescriptorImpl();

  @Extension
  public static class DescriptorImpl extends Descriptor<MoUserAuth> {}
}
