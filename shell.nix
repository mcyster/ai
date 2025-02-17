let
  pkgs = import <nixpkgs> { config = { allowUnfree = true; }; };

in
  pkgs.mkShell {
    buildInputs = with pkgs; [
      less
      vim
      jq.bin
      jdk21
      ngrok
      oauth2l
      google-chrome
      chromedriver
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

      mkdir -p $HOME/.nix-profile/bin
      ln -sf ${pkgs.google-chrome}/bin/google-chrome-stable $HOME/.nix-profile/bin/google-chrome
    '';
  }
