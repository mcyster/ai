let
  pkgs = import <nixpkgs> { config = { allowUnfree = true; }; };
in
pkgs.mkShell {
  buildInputs = [
    pkgs.less
    pkgs.vim
    pkgs.jq.bin
    pkgs.jdk21
    pkgs.ngrok
    pkgs.oauth2l
    pkgs.google-chrome
    pkgs.chromedriver
    pkgs.fontconfig
    pkgs.freetype
    pkgs.xorg.libX11
    pkgs.xorg.libXrender
    pkgs.xorg.libXext
    pkgs.glib
    pkgs.nss
    pkgs.gtk3
    pkgs.wayland
    pkgs.libxkbcommon
  ];

  LANG = "en_US.UTF-8";
  LC_ALL = "en_US.UTF-8";
  LOCALE_ARCHIVE = "${pkgs.glibcLocales}/lib/locale/locale-archive";

  shellHook = ''
    export AI_HOME=${builtins.getEnv "PWD"}
    if [ -f $HOME/.ai-rc ]; then
      source $HOME/.ai-rc
    fi
    export SPRING_AI_OPENAI_API_KEY=$OPENAI_API_KEY
    export LANG=en_US.UTF-8

    # Enable Wayland support for Chrome
    export NIXOS_OZONE_WL=1

    # Create a symlink named 'google-chrome' pointing to 'google-chrome-stable'
    mkdir -p $HOME/.nix-profile/bin
    ln -sf ${pkgs.google-chrome}/bin/google-chrome-stable $HOME/.nix-profile/bin/google-chrome

    # Set the CHROMEDRIVER environment variable
    export CHROMEDRIVER=${pkgs.chromedriver}/bin/chromedriver

    # Ensure Chrome can access the necessary fonts
    export FONTCONFIG_FILE=$HOME/.nix-profile/etc/fonts/fonts.conf
  '';
}

