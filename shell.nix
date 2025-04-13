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
    pkgs.noto-fonts  # Comprehensive font support
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
  '';
}

