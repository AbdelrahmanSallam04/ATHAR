package com.nonprofit.controller;

/**
 * Implemented by screen controllers that need the shell, e.g. to navigate to
 * another sidebar item or refresh the user info in the sidebar.
 * ShellController passes itself in right after loading the screen.
 */
public interface ShellAware {

    void setShell(ShellController shell);
}
