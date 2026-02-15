# Brainstorming-Workshop: Familien-Routineplaner App

## App-Arbeitstitel: **RoutineHeld**

---

## 1. Workshop-Teilnehmer (Rollen)

| Rolle | Kürzel | Perspektive |
|-------|--------|-------------|
| Product Owner | **PO** | Geschäftswert, Priorisierung, MVP-Scope |
| UX-Designerin | **UX** | Benutzerfreundlichkeit, visuelle Gestaltung, Eltern-Workflow |
| Mobile-Entwickler | **DEV** | Technische Machbarkeit, Android-Architektur, Performance |
| QA-Ingenieur | **QA** | Testbarkeit, Edge Cases, Abnahmekriterien |
| Eltern-Vertreterin | **USER** | Alltagstauglichkeit, echte Pain Points, Wünsche |

---

## 2. Workshop-Diskussion

### Runde 1: Kernproblem und Zielgruppe

**USER:** *Das Hauptproblem im Alltag mit kleinen Kindern ist die Vorhersehbarkeit. Kinder brauchen Struktur, aber starre Zeitpläne funktionieren selten. Was wirklich hilft, sind visuelle Abläufe: „Erst kommt X, dann Y, dann Z." Mein Kind muss den Plan nicht lesen können — es reicht, wenn es die Bilder erkennt.*

**UX:** *Genau das sehe ich in den Referenzbildern. Die erfolgreichen Produkte auf Etsy arbeiten alle mit großen, klaren Symbolen und wenig Text. Der Elternteil erstellt den Plan digital, aber das Kind interagiert ggf. mit einem ausgedruckten oder angezeigten Plan. Deshalb ist der PDF-Export so zentral — viele Familien hängen den Plan an den Kühlschrank.*

**PO:** *Wichtiger Punkt. Unsere Primärnutzer sind die Eltern, nicht die Kinder. Die App muss also schnell und effizient bedienbar sein — Eltern haben wenig Zeit. Der MVP sollte den kürzesten Weg von „App öffnen" zu „Plan am Kühlschrank hängen" bieten.*

**DEV:** *Technisch heißt das: lokale Datenhaltung als Priorität, kein Login-Zwang im MVP. Wir sollten mit Room-Datenbank und einer sauberen MVVM-Architektur starten. PDF-Generierung kann über die Android-eigene PdfDocument-API oder eine Bibliothek wie iText laufen.*

**QA:** *Für die Abnahme brauchen wir klare Definitionen: Wie viele Aktivitäten pro Plan? Gibt es ein Maximum? Wie verhält sich Drag & Drop bei langen Listen? Was passiert beim Drehen des Bildschirms mitten im Drag-Vorgang?*

### Runde 2: Feature-Ideenfindung

**USER:** *Was mir bei bestehenden Lösungen fehlt: Ich kaufe auf Etsy Routinekarten als PDF, drucke sie aus, laminiere sie — und wenn sich etwas ändert, fange ich von vorn an. Eine App, in der ich Pläne schnell anpassen und neu drucken kann, wäre Gold wert. Außerdem wäre es toll, zwischen Werktag- und Wochenend-Routinen zu wechseln.*

**UX:** *Dazu noch eine Idee: Vorlagen! Nicht jede Familie will bei Null anfangen. Wir könnten „Morgenroutine 3–5 Jahre", „Abendroutine Schulkind" etc. als Startpunkt anbieten. Außerdem sehe ich in den Referenzen Belohnungssysteme (Punktepläne) — das wäre ein starkes Feature für eine spätere Version.*

**PO:** *Vorlagen sind clever, weil sie das Onboarding beschleunigen und den „Time to Value" massiv verkürzen. Für den MVP würde ich 5–8 Vorlagen als Startkonfiguration mitliefern. Das Belohnungssystem ist spannend, aber klar Post-MVP.*

**DEV:** *Bei den Symbolen sehe ich zwei Wege: Entweder eine fest eingebaute Icon-Bibliothek (kontrolliert, schnell, offline) oder Nutzern erlauben, eigene Fotos als Symbole zu verwenden. Für den MVP würde ich mit 40–60 vordefinierten Icons starten und eigene Fotos als zweites Feature nachziehen.*

**QA:** *Wichtig bei eigenen Fotos: Bildgrößen begrenzen, Seitenverhältnisse normalisieren, und im PDF müssen die Bilder sauber gerendert werden. Das ist fehleranfällig — spricht für die Priorisierung als Post-MVP.*

### Runde 3: Wochenplan und Tagesstruktur

**USER:** *Der Wochenplan ist für mich fast wichtiger als der Tagesablauf. Montags hat mein Kind Turnen, Dienstags Kindergarten bis 16 Uhr, Mittwochs ist Papa-Tag. Das muss ich schnell überblicken können. Ideal wäre eine Wochenansicht mit Morgen/Mittag/Abend-Slots, wie in den Referenzbildern.*

**UX:** *Die Wochenplanansicht sollte als Matrix funktionieren: Spalten für Tage, Zeilen für Tageszeiten. Jede Zelle kann einen Mini-Ablaufplan oder einzelne Aktivitäten enthalten. Für den Export brauchen wir ein separates PDF-Layout — Querformat für den Wochenplan, Hochformat für Tagesabläufe.*

**DEV:** *Datenmodell-technisch ist der Wochenplan eine Komposition: Er referenziert Tagesabläufe pro Tag und Tageszeit. Wir brauchen also drei Entitäten: Aktivität, Ablaufplan (geordnete Liste von Aktivitäten), und Wochenplan (Matrix aus Ablaufplänen). Das ist sauber trennbar.*

**PO:** *Genau, und das beeinflusst die Implementierungsreihenfolge: Erst Aktivitäten, dann Ablaufpläne, dann Wochenplan — jede Schicht baut auf der vorherigen auf.*

### Runde 4: Zusätzliche Feature-Ideen

**UX:** *Farb-Theming! Die Referenzbilder zeigen, wie wichtig die Farbpalette ist — warme, kindgerechte Töne (siehe die Farbpaletten in den Referenzen). Eltern sollten zwischen 3–4 Farbthemen wählen können, damit der ausgedruckte Plan zur Kinderzimmer-Ästhetik passt.*

**USER:** *Könnte die App mich morgens an den Tagesplan erinnern? Eine kleine Benachrichtigung „Luisas Morgenroutine steht an" wäre hilfreich. Und: Wenn beide Elternteile die App nutzen, sollten die Pläne synchronisierbar sein.*

**DEV:** *Benachrichtigungen sind technisch einfach über WorkManager/AlarmManager. Cloud-Sync ist dagegen ein großes Thema — das erfordert Backend-Infrastruktur, Auth, Conflict Resolution. Für den MVP würde ich stattdessen einen JSON-Export/Import anbieten, damit Pläne zwischen Geräten geteilt werden können.*

**QA:** *Guter Punkt. Beim Export/Import müssen wir Versionierung beachten: Was passiert, wenn ein Plan importiert wird, der Icons referenziert, die auf dem Zielgerät nicht vorhanden sind? Fallback-Icons brauchen wir auf jeden Fall.*

**PO:** *Zusammenfassung der neuen Feature-Ideen, die ich notiert habe:*

---

## 3. Feature-Katalog (priorisiert)

### MVP-Features (Must Have)

| # | Feature | Beschreibung |
|---|---------|-------------|
| F1 | Aktivitätenverwaltung | Erstellen, bearbeiten, löschen von Aktivitäten mit Name, Icon und optionaler Farbe |
| F2 | Icon-Bibliothek | 40–60 vordefinierte kindgerechte Icons für typische Tagesaktivitäten |
| F3 | Ablaufplan erstellen | Geordnete Liste von Aktivitäten per Drag & Drop zusammenstellen |
| F4 | Ablaufplan bearbeiten | Reihenfolge ändern, Aktivitäten hinzufügen/entfernen, Plan umbenennen |
| F5 | Wochenplan erstellen | Matrix-Ansicht (7 Tage × 3 Tageszeiten) mit Zuordnung von Aktivitäten/Abläufen |
| F6 | PDF-Export | Ablaufplan und Wochenplan als druckfertiges PDF exportieren |
| F7 | Vorlagen | 5–8 vorgefertigte Ablaufpläne als Startpunkt |

### Post-MVP Features (Should Have)

| # | Feature | Beschreibung |
|---|---------|-------------|
| F8 | Eigene Fotos als Icons | Kamera/Galerie-Integration für personalisierte Aktivitäts-Symbole |
| F9 | Farbthemen | 3–4 wählbare Farbpaletten für App und PDF-Ausgabe |
| F10 | Benachrichtigungen | Tägliche Erinnerung an den aktuellen Tagesplan |
| F11 | Plan-Export/Import (JSON) | Pläne als Datei teilen und auf anderem Gerät importieren |
| F12 | Belohnungssystem | Punkteplan mit Sticker-/Stempel-Logik pro erledigte Aktivität |

### Nice-to-Have Features (Could Have)

| # | Feature | Beschreibung |
|---|---------|-------------|
| F13 | Cloud-Synchronisation | Echtzeit-Sync zwischen Geräten beider Elternteile |
| F14 | Timer pro Aktivität | Optionale Zeitdauer pro Aktivität mit Countdown |
| F15 | Kinderansicht (Read-Only) | Vereinfachte Vollbildansicht des aktuellen Plans für das Kind am Tablet |
| F16 | Sprachausgabe | Aktivitätsnamen vorlesen (TalkBack-Integration + eigene TTS) |
| F17 | Saisonale Icon-Packs | Zusätzliche Icons für Jahreszeiten, Feiertage, Urlaub |
| F18 | Mehrere Kinder/Profile | Separate Pläne pro Kind mit eigenem Farbprofil |
| F19 | Widget | Homescreen-Widget mit dem heutigen Tagesplan |
| F20 | Wiederholungslogik | Wochenplan automatisch jede Woche wiederholen mit Ausnahmen |

---

## 4. Arbeitspakete

### AP-01: Projektsetup und Architektur

**Beschreibung:** Einrichtung des Android-Projekts mit moderner Architektur. Kotlin, Jetpack Compose als UI-Framework, Room-Datenbank für lokale Persistenz, MVVM-Architekturpattern mit Repository-Schicht. Navigation via Compose Navigation. Grundlegende Ordnerstruktur, Build-Konfiguration, und CI-Pipeline-Grundlage.

**Abnahmekriterien:**
- Das Projekt kompiliert fehlerfrei mit Kotlin und Jetpack Compose.
- Die Room-Datenbank ist konfiguriert und enthält leere Entity-Definitionen für Aktivität, Ablaufplan und Wochenplan.
- Eine Compose-Navigation mit Bottom-Navigation-Bar zeigt drei leere Screens (Aktivitäten, Ablaufpläne, Wochenplan).
- MVVM-Architektur ist durch mindestens ein ViewModel + Repository-Paar nachgewiesen.
- Die App startet auf einem Emulator (API Level 26+) ohne Crash.
- Eine README dokumentiert die Architekturentscheidungen.

**Komplexität:** 🟡 Mittel (3/5) — Architekturentscheidungen erfordern Erfahrung, Implementierung ist aber standardisiert.

---

### AP-02: Datenmodell und Datenbank

**Beschreibung:** Definition und Implementierung des vollständigen Datenmodells in Room. Entitäten: Activity (id, name, iconRef, color, createdAt), RoutinePlan (id, name, type, createdAt), RoutinePlanEntry (id, planId, activityId, position), WeekPlan (id, name), WeekPlanSlot (id, weekPlanId, dayOfWeek, timeOfDay, routinePlanId). Inklusive DAOs für CRUD-Operationen, Migrationskonzept und Unit-Tests.

**Abnahmekriterien:**
- Alle fünf Entitäten sind als Room-Entities mit korrekten Relationen (Foreign Keys) implementiert.
- DAOs bieten CRUD-Operationen für alle Entitäten mit Flow-basiertem Lesen.
- Positionen in RoutinePlanEntry sind als Integer-Werte gespeichert und erlauben Neuordnung.
- Unit-Tests (mind. 15 Tests) decken alle CRUD-Operationen und Beziehungen ab.
- Ein Datenbank-Migrationstest existiert (auch wenn noch keine Migration nötig ist).
- Seed-Daten für die Icon-Referenzen sind definiert.

**Komplexität:** 🟡 Mittel (3/5) — Standardmäßiges Room-Setup, aber die Relationen (Plan → Entries → Activities, WeekPlan → Slots → Plans) erfordern sorgfältiges Design.

---

### AP-03: Icon-Bibliothek und Asset-Management

**Beschreibung:** Bereitstellung einer Bibliothek von 40–60 kindgerechten Vektor-Icons (SVG/Vector Drawable) für typische Tagesaktivitäten. Kategorien: Morgenroutine (Aufwachen, Zähneputzen, Anziehen…), Mahlzeiten (Frühstück, Mittagessen, Snack…), Aktivitäten (Spielen, Lernen, Sport…), Abendroutine (Baden, Schlafanzug, Buch lesen…), Orte (Kindergarten, Schule, Oma…), Sonstiges (Einkaufen, Arzt, Ausflug…). Inklusive Suchfunktion und Kategoriefilter.

**Abnahmekriterien:**
- Mindestens 50 Icons sind als Android Vector Drawables im Projekt vorhanden.
- Jedes Icon hat einen deutschen Namen und eine Kategorie-Zuordnung.
- Ein IconPicker-Composable zeigt alle Icons in einem Grid mit Kategoriefilter-Chips an.
- Icons werden in Suchergebnissen korrekt nach Name gefiltert (Eingabe „Zähne" findet „Zähne putzen").
- Alle Icons sind im selben visuellen Stil gehalten (einheitliche Strichstärke, Farbigkeit, Proportionen).
- Icons rendern korrekt in Größen von 24dp bis 120dp.

**Komplexität:** 🟡 Mittel (3/5) — Technisch unkompliziert, aber die Asset-Erstellung oder -Beschaffung und Konsistenzprüfung ist aufwändig.

---

### AP-04: Aktivitätenverwaltung (CRUD-Screen)

**Beschreibung:** Vollständiger Screen für die Verwaltung eigener Aktivitäten. Liste aller vorhandenen Aktivitäten mit Icon-Vorschau und Name. Dialog/Sheet zum Erstellen und Bearbeiten: Name eingeben, Icon aus der Bibliothek wählen, optionale Farbzuordnung. Löschen mit Bestätigungsdialog. Prüfung auf Verwendung in bestehenden Plänen vor dem Löschen.

**Abnahmekriterien:**
- Der Aktivitäten-Screen zeigt alle Aktivitäten in einer scrollbaren Liste mit Icon und Name.
- Ein FAB (Floating Action Button) öffnet einen Dialog zum Erstellen einer neuen Aktivität.
- Im Erstellungsdialog kann der Nutzer einen Namen eingeben (max. 30 Zeichen) und ein Icon auswählen.
- Optional kann eine von 8 Farben zugewiesen werden.
- Tippen auf eine bestehende Aktivität öffnet den Bearbeitungsdialog mit vorausgefüllten Werten.
- Beim Löschen erscheint ein Bestätigungsdialog. Falls die Aktivität in einem Plan verwendet wird, wird darauf hingewiesen.
- Leerer Zustand: Ein Platzhalter-Text/Bild erscheint, wenn keine Aktivitäten vorhanden sind.
- Alle Aktionen persistieren sofort in der Room-Datenbank.

**Komplexität:** 🟢 Niedrig-Mittel (2/5) — Standard-CRUD-UI mit Compose, IconPicker-Integration ist die Hauptarbeit.

---

### AP-05: Ablaufplan-Erstellung mit Drag & Drop

**Beschreibung:** Kernfeature der App. Ein Ablaufplan besteht aus einer vertikalen Liste von Plätzen (Slots). Der Nutzer erstellt einen neuen Plan mit Namen und Anzahl der Slots (3–12). Die leeren Slots werden als nummerierte Platzhalter dargestellt. Per Drag & Drop werden Aktivitäten aus einem seitlichen oder unteren Auswahlbereich auf die Slots gezogen. Alternativ kann ein Slot angetippt werden, um eine Aktivität aus einer Liste zuzuweisen. Die Reihenfolge der gefüllten Slots ist per Drag & Drop änderbar.

**Abnahmekriterien:**
- Ein „Neuer Plan"-Flow fragt nach Name und Anzahl der Plätze (3–12, per Slider oder Zahleneingabe).
- Der Plan-Editor zeigt nummerierte Slots vertikal an. Leere Slots haben ein „+"-Symbol und gestrichelte Umrandung.
- Ein ausklappbarer Aktivitäten-Bereich (Bottom Sheet oder Side Panel) zeigt verfügbare Aktivitäten.
- Eine Aktivität kann per Drag & Drop auf einen leeren Slot gezogen werden, alternativ per Tippen auf den Slot.
- Gefüllte Slots zeigen Nummer, Icon und Aktivitätsname.
- Die Reihenfolge gefüllter Slots ist per Long-Press + Drag änderbar (Nummern aktualisieren sich automatisch).
- Ein gefüllter Slot kann per Wischgeste oder Menü geleert werden.
- Der Plan wird automatisch gespeichert (Auto-Save bei jeder Änderung).
- Beim Drehen des Bildschirms bleibt der Zustand vollständig erhalten.
- Performance: Drag & Drop bleibt bei 12 Slots flüssig (60 fps).

**Komplexität:** 🔴 Hoch (5/5) — Drag & Drop in Compose ist technisch anspruchsvoll (LazyColumn-Reordering, Drop-Target-Erkennung, Animationen). Dies ist das komplexeste Arbeitspaket.

---

### AP-06: Ablaufplan-Übersicht und -Verwaltung

**Beschreibung:** Übersichtsscreen aller erstellten Ablaufpläne. Jeder Plan wird als Karte mit Name, Vorschau der ersten 3–4 Icons und Erstellungsdatum dargestellt. Funktionen: Plan öffnen/bearbeiten, duplizieren, löschen, als PDF exportieren. Sortierung nach Name oder Datum.

**Abnahmekriterien:**
- Alle Ablaufpläne werden als Karten in einer scrollbaren Liste angezeigt.
- Jede Karte zeigt: Planname, Vorschau-Icons (max. 4), Anzahl der Aktivitäten, Erstellungsdatum.
- Tippen öffnet den Plan im Editor (AP-05).
- Long-Press oder 3-Punkt-Menü bietet: Bearbeiten, Duplizieren, Als PDF exportieren, Löschen.
- Duplizieren erstellt eine exakte Kopie mit dem Suffix „(Kopie)".
- Löschen erfordert Bestätigung.
- Eine Suchleiste filtert Pläne nach Name.
- Leerer Zustand zeigt einen hilfreichen Hinweis mit Illustration und CTA-Button „Ersten Plan erstellen".

**Komplexität:** 🟢 Niedrig (2/5) — Standard-Listenansicht mit Compose, Wiederverwendung bestehender Komponenten.

---

### AP-07: PDF-Export (Ablaufplan)

**Beschreibung:** Export eines Ablaufplans als druckfertiges PDF im Hochformat (A4). Layout: Titel oben, darunter die Aktivitäten als nummerierte Karten (Icon + Name) in einem 3- oder 4-Spalten-Grid. Kindgerechtes Design mit abgerundeten Ecken, Farben aus dem gewählten Theme, und optionalem Kindnamen als Header. Das PDF wird im Download-Ordner gespeichert und kann direkt geteilt werden (Share Intent).

**Abnahmekriterien:**
- Der Export erzeugt ein valides PDF-Dokument im A4-Hochformat.
- Das PDF enthält: optionaler Titel/Kindname, alle Aktivitäten des Plans als nummerierte Karten mit Icon und Name.
- Icons werden korrekt als Vektorgrafiken oder hochauflösende Bitmaps (mind. 300dpi-Äquivalent) gerendert.
- Das Layout passt sich der Anzahl der Aktivitäten an (3–4 Spalten, automatischer Seitenumbruch ab 12+ Aktivitäten).
- Das Farbschema des PDFs entspricht dem gewählten App-Theme.
- Nach dem Export wird ein Share-Dialog angeboten (Drucken, WhatsApp, E-Mail etc.).
- Das PDF wird zusätzlich im Downloads-Ordner gespeichert.
- Der Export-Vorgang zeigt einen Ladeindikator und dauert maximal 3 Sekunden.

**Komplexität:** 🟡 Mittel-Hoch (4/5) — PDF-Layout-Programmierung ist fummelig, insbesondere korrekte Icon-Einbettung und Seitenlayout.

---

### AP-08: Wochenplan-Erstellung

**Beschreibung:** Matrix-basierter Wochenplan. Spalten: Montag bis Sonntag. Zeilen: Morgens, Mittags, Abends. Jede Zelle kann einen bestehenden Ablaufplan referenzieren oder einzelne Aktivitäten enthalten. Die Ansicht ist horizontal scrollbar (Tage) mit fixierter Zeilen-Header-Spalte. Tippen auf eine Zelle öffnet einen Auswahldialog für vorhandene Ablaufpläne oder eine Schnelleingabe für einzelne Aktivitäten.

**Abnahmekriterien:**
- Der Wochenplan zeigt eine 7×3-Matrix (Tage × Tageszeiten) an.
- Die Tageszeiten-Spalte (Morgens/Mittags/Abends) ist beim horizontalen Scrollen fixiert.
- Tippen auf eine leere Zelle öffnet einen Dialog mit zwei Optionen: „Ablaufplan zuweisen" oder „Einzelne Aktivität hinzufügen".
- Zugewiesene Ablaufpläne werden als Mini-Vorschau (Icons) in der Zelle dargestellt.
- Einzelne Aktivitäten zeigen ihr Icon in der Zelle.
- Eine zugewiesene Zelle kann per Long-Press geleert oder geändert werden.
- Der Wochenplan wird automatisch gespeichert.
- Mindestens zwei Wochenpläne können nebeneinander existieren (z.B. „Normaler Wochentag" und „Ferienplan").

**Komplexität:** 🔴 Hoch (4/5) — Komplexe UI-Komposition (fixierter Header, horizontales Scrollen, Matrix-Layout) und verschachteltes Datenmodell.

---

### AP-09: PDF-Export (Wochenplan)

**Beschreibung:** Export des Wochenplans als PDF im Querformat (A4). Die 7×3-Matrix wird als Tabelle gerendert mit farbig hinterlegten Tageszeiten-Zeilen. Jede Zelle zeigt die zugewiesenen Icons. Titel und optionaler Kindname als Header.

**Abnahmekriterien:**
- Das PDF wird im A4-Querformat erzeugt.
- Die Matrix ist als lesbare Tabelle dargestellt mit beschrifteten Spalten (Mo–So) und Zeilen (Morgens/Mittags/Abends).
- Jede Zelle enthält die zugewiesenen Icons (max. 4 Icons pro Zelle, bei mehr ein „+X"-Indikator).
- Tageszeiten sind farblich hinterlegt (z.B. Gelb für Morgens, Grün für Mittags, Blau für Abends).
- Der Share-Intent funktioniert wie in AP-07.
- Bei leeren Zellen wird ein dezenter Platzhalter angezeigt (kein leeres Feld).

**Komplexität:** 🟡 Mittel (3/5) — Ähnliche Mechanik wie AP-07, aber komplexeres Tabellenlayout.

---

### AP-10: Vorlagen-System

**Beschreibung:** Mitlieferung von 5–8 vorgefertigten Ablaufplan-Vorlagen. Vorlagen sind beim ersten App-Start verfügbar und als solche gekennzeichnet. Der Nutzer kann eine Vorlage als Basis für einen eigenen Plan verwenden („Als Kopie verwenden"). Vorlagen: Morgenroutine Kleinkind, Morgenroutine Schulkind, Abendroutine, Wochenend-Morgen, Kindergarten-Tag, Einkaufen mit Kind, Hausaufgaben-Routine, Bettgeh-Routine.

**Abnahmekriterien:**
- Beim ersten Start der App (oder nach Datenbank-Reset) sind 6–8 Vorlagen vorhanden.
- Vorlagen sind in der Ablaufplan-Übersicht visuell als „Vorlage" gekennzeichnet (Badge oder Icon).
- Vorlagen können nicht direkt bearbeitet oder gelöscht werden.
- Tippen auf eine Vorlage öffnet eine Vorschau mit „Als Kopie verwenden"-Button.
- Die Kopie ist ein vollständig editierbarer eigener Plan.
- Vorlagen referenzieren nur Icons aus der Basis-Bibliothek (AP-03).
- Die Vorlagen sind in einer JSON- oder Seed-Datei definiert, die einfach erweiterbar ist.

**Komplexität:** 🟢 Niedrig (2/5) — Datenpflege und einfache UI-Logik, keine neue Architektur nötig.

---

### AP-11: Onboarding und Guided Tour

**Beschreibung:** Beim ersten Start der App wird der Nutzer durch eine kurze Einführung geleitet (3–4 Screens). Anschließend wird er direkt in die Erstellung des ersten Plans geführt — entweder aus einer Vorlage oder als leerer Plan. Ziel: Der Nutzer hat nach maximal 2 Minuten seinen ersten ausdruckbaren Plan.

**Abnahmekriterien:**
- Beim allerersten App-Start erscheinen 3–4 Onboarding-Screens (horizontal swipebar).
- Inhalte: (1) Willkommen + Wertversprechen, (2) Erklärung Ablaufplan, (3) Erklärung Wochenplan, (4) „Los geht's"-CTA.
- Nach dem Onboarding wird der Nutzer direkt zur Vorlagenauswahl oder Plan-Erstellung geleitet.
- Das Onboarding kann übersprungen werden.
- Das Onboarding erscheint nur einmal (Flag in SharedPreferences).
- Ein „Hilfe"-Eintrag in den Einstellungen erlaubt, das Onboarding erneut anzuzeigen.

**Komplexität:** 🟢 Niedrig (1/5) — Standard HorizontalPager mit Compose, minimale Logik.

---

### AP-12: Einstellungen und App-Konfiguration

**Beschreibung:** Einstellungsscreen mit folgenden Optionen: Kindname(n) für PDF-Header, Standard-Anzahl Slots für neue Pläne, Über die App / Impressum, Datenschutzhinweis, Onboarding erneut anzeigen, Alle Daten löschen (mit Bestätigung). Zukunftssicher: Platzhalter für Farbthema-Auswahl und Benachrichtigungs-Einstellungen.

**Abnahmekriterien:**
- Ein Einstellungs-Screen ist über die Bottom-Navigation oder ein Menü erreichbar.
- Der Kindname kann eingegeben und gespeichert werden (erscheint im PDF-Header).
- Die Standard-Slot-Anzahl für neue Pläne ist konfigurierbar (3–12).
- „Über die App" zeigt Version, Entwickler-Info und Lizenzen.
- „Alle Daten löschen" erfordert eine zweistufige Bestätigung und löscht alle Pläne, Aktivitäten und Wochenpläne.
- Einstellungen werden über DataStore (Preferences) persistiert.

**Komplexität:** 🟢 Niedrig (1/5) — Einfacher Settings-Screen, kein komplexer State.

---

### AP-13: Eigene Fotos als Aktivitäts-Icons (Post-MVP)

**Beschreibung:** Erweiterung der Aktivitätenverwaltung um die Möglichkeit, eigene Fotos als Icons zu verwenden. Integration von Kamera und Galerie-Zugriff. Fotos werden quadratisch zugeschnitten, auf eine einheitliche Größe skaliert und lokal gespeichert. Im PDF-Export werden Fotos als Bitmap gerendert.

**Abnahmekriterien:**
- Im Icon-Picker gibt es eine zusätzliche Option „Eigenes Foto".
- Tippen öffnet eine Auswahl: Kamera oder Galerie.
- Das gewählte Foto wird in einem quadratischen Crop-Dialog angezeigt.
- Das zugeschnittene Foto wird auf 256×256px skaliert und im App-internen Speicher abgelegt.
- Das Foto-Icon wird in der Aktivitätenliste, im Plan-Editor und im PDF korrekt dargestellt.
- Bei Deinstallation oder Datenlöschung werden die Fotos mitgelöscht.
- Maximal 30 eigene Fotos sind erlaubt (Speicherbegrenzung).

**Komplexität:** 🟡 Mittel (3/5) — Kamera/Galerie-Intents, Cropping, Speicherverwaltung, PDF-Integration.

---

### AP-14: Farbthemen (Post-MVP)

**Beschreibung:** Der Nutzer kann zwischen 4 vordefinierten Farbthemen wählen. Die Themes beeinflussen die App-UI und den PDF-Export. Themes basierend auf den Referenz-Farbpaletten: „Ozean" (Blaugrau/Salbei), „Honig" (Warmgelb/Beige), „Sunset" (Pfirsich/Rosa), „Neutral" (Creme/Brauntöne).

**Abnahmekriterien:**
- In den Einstellungen kann eines von 4 Farbthemen gewählt werden.
- Die Auswahl wird als Vorschau-Karte mit den Theme-Farben dargestellt.
- Die App-UI (Hintergrund, Karten, Navigation) passt sich dem gewählten Theme an.
- PDFs werden im gewählten Theme-Farbschema exportiert.
- Das Standard-Theme ist „Honig".
- Der Theme-Wechsel erfordert keinen App-Neustart.

**Komplexität:** 🟡 Mittel (3/5) — Dynamisches Theming in Compose ist gut unterstützt, aber PDF-Farbanpassung und konsistente Durchführung sind aufwändig.

---

### AP-15: Benachrichtigungen (Post-MVP)

**Beschreibung:** Optionale tägliche Erinnerung an den Tagesplan. Der Nutzer kann eine Uhrzeit und Wochentage konfigurieren. Die Benachrichtigung zeigt den Plan-Namen und eine Vorschau der ersten 3 Aktivitäten.

**Abnahmekriterien:**
- In den Einstellungen kann eine tägliche Erinnerung aktiviert werden.
- Uhrzeit (Standard: 7:00) und Wochentage sind konfigurierbar.
- Die Benachrichtigung enthält den Plan-Namen und die Icons der ersten 3 Aktivitäten.
- Tippen auf die Benachrichtigung öffnet den entsprechenden Plan in der App.
- Die Benachrichtigung funktioniert auch nach Geräte-Neustart (BroadcastReceiver für BOOT_COMPLETED).
- Bei deaktivierter Erinnerung werden alle geplanten Benachrichtigungen entfernt.

**Komplexität:** 🟡 Mittel (2/5) — Standard-Android-Notifications mit AlarmManager, aber zuverlässige Auslieferung über Doze Mode hinweg erfordert Sorgfalt.

---

### AP-16: JSON-Export/Import (Post-MVP)

**Beschreibung:** Export aller Pläne (oder einzelner Pläne) als JSON-Datei. Import von JSON-Dateien mit Validierung und Konfliktlösung (Aktivitäten mit gleichem Namen zusammenführen oder duplizieren).

**Abnahmekriterien:**
- In der Plan-Übersicht gibt es eine Option „Alle exportieren" und pro Plan „Exportieren".
- Der Export erzeugt eine valide JSON-Datei mit allen referenzierten Aktivitäten und dem Plan.
- Ein Share-Intent bietet Speichern/Teilen der JSON-Datei an.
- Der Import erkennt JSON-Dateien über eine File-Intent-Registrierung oder einen manuellen Import-Button.
- Bei Namenskonflikten (gleicher Aktivitätsname existiert bereits) wird ein Dialog angeboten: Zusammenführen oder Duplizieren.
- Ungültige oder beschädigte JSON-Dateien erzeugen eine verständliche Fehlermeldung.
- Die JSON-Struktur ist versioniert (Versionsfeld im Root-Objekt).

**Komplexität:** 🟡 Mittel (3/5) — Serialisierung ist einfach, aber Konfliktlösung und robustes Error-Handling erfordern Sorgfalt.

---

## 5. Komplexitätsübersicht

| AP | Name | Komplexität | Aufwand (Story Points) |
|----|------|-------------|----------------------|
| AP-01 | Projektsetup und Architektur | 🟡 3/5 | 8 |
| AP-02 | Datenmodell und Datenbank | 🟡 3/5 | 5 |
| AP-03 | Icon-Bibliothek | 🟡 3/5 | 8 |
| AP-04 | Aktivitätenverwaltung | 🟢 2/5 | 5 |
| AP-05 | Ablaufplan mit Drag & Drop | 🔴 5/5 | 13 |
| AP-06 | Ablaufplan-Übersicht | 🟢 2/5 | 3 |
| AP-07 | PDF-Export (Ablaufplan) | 🟡 4/5 | 8 |
| AP-08 | Wochenplan-Erstellung | 🔴 4/5 | 13 |
| AP-09 | PDF-Export (Wochenplan) | 🟡 3/5 | 5 |
| AP-10 | Vorlagen-System | 🟢 2/5 | 3 |
| AP-11 | Onboarding | 🟢 1/5 | 2 |
| AP-12 | Einstellungen | 🟢 1/5 | 2 |
| AP-13 | Eigene Fotos als Icons | 🟡 3/5 | 5 |
| AP-14 | Farbthemen | 🟡 3/5 | 5 |
| AP-15 | Benachrichtigungen | 🟡 2/5 | 3 |
| AP-16 | JSON-Export/Import | 🟡 3/5 | 5 |

**Gesamt MVP (AP-01 bis AP-12):** 75 Story Points
**Gesamt Post-MVP (AP-13 bis AP-16):** 18 Story Points

---

## 6. Implementierungsreihenfolge

Die Reihenfolge folgt dem Prinzip der technischen Abhängigkeiten und des inkrementellen Nutzwertes. Jede Phase liefert ein testbares Inkrement.

### Phase 1: Fundament (AP-01, AP-02)
Architektur und Datenmodell bilden die Grundlage für alles Weitere. Ohne diese kann kein Feature implementiert werden.

**Ergebnis:** Lauffähige App-Hülle mit leeren Screens und funktionierender Datenbank.

### Phase 2: Aktivitäten-Kern (AP-03, AP-04)
Die Icon-Bibliothek und Aktivitätenverwaltung sind Voraussetzung für jeden Plan. Ohne Aktivitäten gibt es nichts zum Planen.

**Ergebnis:** Der Nutzer kann Aktivitäten mit Icons erstellen und verwalten.

### Phase 3: Ablaufplan (AP-05, AP-06)
Das Herzstück der App. AP-05 ist das komplexeste Paket und sollte früh angegangen werden, damit Risiken früh sichtbar werden. AP-06 liefert die Verwaltungsebene.

**Ergebnis:** Vollständige Ablaufplan-Erstellung und -Verwaltung per Drag & Drop.

### Phase 4: Erster Export (AP-07)
Sobald Ablaufpläne existieren, wird der PDF-Export implementiert. Dies schließt den Kernwert-Loop: Erstellen → Exportieren → Ausdrucken.

**Ergebnis:** Erster druckbarer Plan. MVP-Kernwert ist erlebbar.

### Phase 5: Wochenplan (AP-08, AP-09)
Der Wochenplan baut auf bestehenden Ablaufplänen auf und erweitert den Funktionsumfang. PDF-Export für den Wochenplan folgt direkt.

**Ergebnis:** Vollständiger Wochenplan mit Export.

### Phase 6: Polish und Onboarding (AP-10, AP-11, AP-12)
Vorlagen verbessern das Onboarding-Erlebnis. Der Onboarding-Flow und die Einstellungen runden den MVP ab.

**Ergebnis:** Release-fertiger MVP.

### Phase 7: Post-MVP Erweiterungen (AP-13 bis AP-16)
In beliebiger Reihenfolge, empfohlen: AP-14 (Farbthemen) → AP-13 (Fotos) → AP-15 (Benachrichtigungen) → AP-16 (JSON-Export).

---

## 7. Technologie-Stack (Empfehlung)

| Bereich | Technologie |
|---------|-------------|
| Sprache | Kotlin |
| UI-Framework | Jetpack Compose |
| Architektur | MVVM + Repository Pattern |
| Lokale Datenbank | Room |
| Preferences | Jetpack DataStore |
| Navigation | Compose Navigation |
| Dependency Injection | Hilt |
| PDF-Generierung | Android PdfDocument API |
| Bildverarbeitung | Coil (Laden) + Android Bitmap API (Cropping) |
| Drag & Drop | Compose Foundation DragAndDrop / LazyList Reorder |
| Testing | JUnit 5 + Compose UI Testing + Espresso |
| Min. SDK | API 26 (Android 8.0) |

---

## 8. Risiken und Offene Punkte

| Risiko | Wahrscheinlichkeit | Impact | Mitigation |
|--------|-------------------|--------|-----------|
| Drag & Drop in Compose ist instabil bei komplexen Listen | Mittel | Hoch | Frühzeitiger Prototyp in Phase 3, ggf. Fallback auf RecyclerView |
| Icon-Beschaffung (Lizenzfragen bei externen Icons) | Hoch | Mittel | Eigene Icons erstellen lassen oder Open-Source-Bibliothek nutzen (z.B. unDraw, OpenMoji) |
| PDF-Layout variiert je nach Geräte-DPI | Mittel | Mittel | Feste PT-basierte Maße statt DP, umfangreiches Testen auf verschiedenen Geräten |
| Performance bei Wochenplan mit vielen zugewiesenen Plänen | Niedrig | Mittel | Lazy Loading der Zell-Inhalte, Caching der Icon-Bitmaps |

---

*Dokument erstellt im Rahmen des Brainstorming-Workshops am 14.02.2026*
*Nächster Schritt: Review und Priorisierungs-Bestätigung durch Product Owner*
